# HomeHub — Plano do Frontend

| Campo | Valor |
|---|---|
| **Status** | Proposta — aguardando decisões da seção 2 |
| **Base analisada** | `main` em `33139f4` (README, AGENTS.md, `docs/homehub-spec.md` e código do backend) |
| **Escopo** | Todo o módulo `homehub-frontend/` e o que ele exige do backend e da infraestrutura |

---

## 1. Ponto de partida

**O frontend ainda não existe no repositório.** Não há `homehub-frontend/`, `package.json`
nem código TypeScript; o que existe é o planejamento (stack no README e na spec §6.2,
roadmap, contratos de API na spec §9). O plano abaixo parte do zero.

O que o backend entrega hoje e o frontend pode consumir:

| Endpoint | Request | Response | Observação |
|---|---|---|---|
| `POST /api/auth/login` | `{ email, password }` | `{ accessToken, refreshToken }` | 401 `USER_LOGIN_INVALID`, 400 `EMAIL_INVALID` / `PASSWORD_INVALID` / `USER_INVALID` (inativo) |
| `GET /api/auth/me` | Bearer | `{ id, email, role }` | `role` é `USER` ou `ADMIN` |
| `POST /api/auth/refresh` | `{ token }` | `{ accessToken }` | 401 `REFRESH_TOKEN_INVALID`; sem rotação (ADR-008) |
| `POST /api/auth/logout` | `{ token }` | 204 | revoga o refresh token |
| `GET /api/auth/verify` | Bearer | 200 / 401 | alvo do Forward Auth; o front não usa |

Formato de erro (`RestError`): `{ errorCode, errorMessage, status, path }`.

**Tudo além disso — usuários, perfil, serviços, sistema — está apenas planejado no
backend** (spec §9.2–9.5). Só a Fase 1 do frontend roda inteira contra o backend atual.

---

## 2. Problemas de arquitetura encontrados

Ordenados por impacto. Cada um traz a correção proposta; os marcados com **decisão**
precisam de resposta antes da fase indicada.

### 2.1 Forward Auth não funciona com navegação do browser — **decisão, bloqueia a Fase 3**

O middleware do Traefik (labels em `application.yml`) repassa só o header
`Authorization` para `/api/auth/verify`. Mas quando o usuário clica em "Jellyfin" no
portal, o browser faz uma navegação comum para `/jellyfin` — **sem header
`Authorization`**. Um SPA não consegue injetar header em navegação, iframe, imagem ou
nos requests que a própria aplicação de destino faz. Resultado: todo serviço protegido
responderia 401, com o usuário logado.

É o problema central do produto ("autenticação única"), e a escolha feita aqui define
onde o frontend guarda a sessão.

**Correção proposta:** o backend passa a emitir o access token também em cookie
`HttpOnly; Secure; SameSite=Lax` no login/refresh, e `/api/auth/verify` passa a aceitar
cookie além do header (o contrato atual por header continua válido — AGENTS §2.10). O
refresh token vai para cookie `HttpOnly` com `Path=/api/auth`. Isso também resolve 2.4.

### 2.2 Roteamento por sub-path quebra parte dos serviços — **decisão, bloqueia a Fase 3**

README e spec roteiam tudo por path (`/photos/*`, `/notes/*`, …) com o frontend em
`/*`. Dois problemas:

- Vários serviços não suportam rodar em sub-path. **Immich e AFFiNE** não suportam;
  Jellyfin, qBittorrent, Grafana e Uptime Kuma exigem configuração de base URL em cada um.
- O catch-all `/*` do SPA disputa com as rotas dos serviços: `/notes` é rota do React
  Router ou do AFFiNE? Qualquer rota nova do front pode colidir com um serviço futuro.

**Correção proposta:** subdomínios (`photos.home.exemplo`, `notes.home.exemplo`, portal
em `home.exemplo`), com o cookie de 2.1 emitido para o domínio pai. Independente da
escolha, o frontend **não codifica URLs de serviço**: cada serviço vem do backend com
sua URL completa, então o front funciona com path ou subdomínio.

### 2.3 Requisição sem autenticação devolve 403, não 401 — correção pequena no backend

`WebSecurityConfig` não define `AuthenticationEntryPoint`; o default do Spring Security
nessa configuração é 403. `WebSecurityConfigIntegrationTest` confirma
(`shouldRequireAuthenticationForAnyOtherEndpoint` espera `isForbidden`). Para o
frontend, "token expirado" e "sem permissão de papel" ficam indistinguíveis.

**Correção proposta:** `authenticationEntryPoint` devolvendo 401 com `RestError`
(`errorCode: UNAUTHENTICATED`) e `accessDeniedHandler` devolvendo 403 `ACCESS_DENIED`.
Hoje essas respostas também não têm corpo `RestError`.

**Enquanto não for corrigido**, o cliente HTTP trata 401 **e** 403 como "tentar refresh
uma vez"; se a repetição ainda der 403, é falta de permissão.

### 2.4 Armazenamento dos tokens no cliente — **decisão, bloqueia a Fase 1**

Os tokens chegam no corpo JSON, então precisam viver em JavaScript. A spec §12.2 já
lista "armazenamento seguro do Refresh Token no cliente" como pendência.

| Opção | Sobrevive a F5 | Exposição a XSS | Exige backend |
|---|---|---|---|
| A. Access em memória + refresh em `localStorage` | sim | refresh token (7 dias) legível por script | não |
| B. Tudo em memória | não — relogar a cada reload | mínima | não |
| C. Cookies `HttpOnly` (2.1) | sim | nenhum token legível | sim |

**Recomendação:** **C** como destino. Para não travar a Fase 1, começar com **A**
isolada atrás de um módulo `tokenStorage` de interface única; a migração para C troca
só esse módulo e o interceptor.

### 2.5 Access token de 24h — ajuste de configuração

`jwt.expiration` é 24h, e a spec §8.2 fala em "curta duração". Com refresh funcionando
no front, 10–15 min é o valor coerente, e reduz o dano de 2.4-A. É uma linha no
`application.yml`; nenhum teste depende do valor.

### 2.6 Erros de login vazam política de senha — tratado no front

Já registrado em AGENTS §9.5. No frontend: o formulário de login **não replica o regex
de senha** e exibe a mesma mensagem genérica para 401 `USER_LOGIN_INVALID`, 400
`EMAIL_INVALID` e 400 `PASSWORD_INVALID`. Só `USER_INVALID` (conta inativa) recebe
mensagem própria. O regex de senha só é usado nas telas de troca/criação de senha.

### 2.7 CORS inexistente — manter assim

Não há configuração de CORS no backend, e não precisa haver: em produção front e API
ficam na mesma origem atrás do Traefik; em desenvolvimento o Vite faz proxy de `/api`
para `:8080`. O cliente HTTP usa sempre URL relativa (`/api/...`). **Não adicionar CORS.**

### 2.8 Stack planejada está defasada — correção de documentação

README e spec §6.2 listam React 18, Vite 5, React Router 6. Proposta para o scaffold:

| Planejado | Proposto | Motivo |
|---|---|---|
| React 18 | React 19 | linha estável atual; shadcn/ui já a tem como alvo |
| Vite 5 | linha estável no dia do scaffold | Vite 5 saiu de suporte |
| React Router 6 | React Router 7 (modo biblioteca/SPA) | sucessor direto, sem SSR |
| Zustand 5 | Zustand 5, **só para estado de cliente** (sessão, tema) | — |
| — | **TanStack Query** para estado de servidor | cache, revalidação, polling de health; evita reimplementar isso em stores |
| — | React Hook Form + Zod | formulários e validação tipada dos contratos |
| — | Testing Library + MSW | testes de componente e mock de API no nível de rede |
| Axios, Tailwind 4, shadcn/ui, Vitest, TS estrito | mantidos | — |

Sem SSR e sem framework full-stack: o alvo é um Raspberry Pi servindo arquivos estáticos.

### 2.9 Itens menores

- `MeResponse` tem só `id`, `email`, `role` e o modelo `User` não tem nome. A UI
  identifica o usuário pelo e-mail até `/api/profile` existir.
- O README linka `homehub-backend/README.md` e `homehub-frontend/README.md`; nenhum existe.
- O CI só dispara em `homehub-backend/**`; falta um workflow do frontend.
- O `.gitignore` da raiz não cobre `node_modules/`, `dist/`, `.env.local`.
- O AGENTS.md só descreve o backend; precisa de uma seção do frontend quando o módulo nascer.

---

## 3. Arquitetura do frontend

Organização por feature, com a mesma disciplina de dependências do backend.

```text
homehub-frontend/
├── index.html
├── vite.config.ts              # proxy /api → http://localhost:8080
├── src/
│   ├── app/                    # composição: providers, router, layouts, guards
│   │   ├── providers.tsx
│   │   ├── router.tsx
│   │   └── layouts/            # AuthLayout, AppLayout (sidebar + header)
│   ├── features/
│   │   ├── auth/               # api/ components/ hooks/ store/ types.ts index.ts
│   │   ├── services/
│   │   ├── profile/
│   │   ├── users/              # área administrativa
│   │   └── system/
│   ├── shared/
│   │   ├── api/                # httpClient (axios), interceptors, ApiError, tokenStorage
│   │   ├── ui/                 # componentes shadcn/ui
│   │   ├── hooks/  lib/  config/
│   └── test/                   # setup do Vitest, handlers MSW, fixtures
└── Dockerfile                  # build multi-stage → nginx:alpine (arm64)
```

### Regras

1. **`shared` não importa `features`; `features` não importam umas às outras** — só via
   o `index.ts` público, e a composição acontece em `app/`. Garantido por regra de lint.
2. **Axios só existe em `shared/api`.** Features declaram funções tipadas em
   `features/<x>/api/` e as expõem por hooks do TanStack Query. Componente não chama HTTP.
3. **Toda resposta de API passa por um schema Zod**; o tipo TypeScript deriva do schema.
   Erros viram `ApiError { code, message, status }` a partir do `RestError`.
4. **A decisão por `errorCode`, nunca por texto de mensagem.**
5. **Autorização por papel em um único lugar**: guard de rota (`RequireRole`) e um
   componente `<Can role="ADMIN">`. O front esconde; quem protege é o backend.
6. **Sessão**: store Zustand `auth` com `status: 'loading' | 'authenticated' | 'anonymous'`
   e `user`. O bootstrap da aplicação tenta refresh + `/me` antes de renderizar rotas.
7. **Refresh com fila única**: várias requisições falhando ao mesmo tempo disparam um só
   `POST /refresh`; as demais aguardam e são repetidas. Falha no refresh → logout local.
8. TypeScript `strict`, sem `any`; URLs de API sempre relativas; textos da UI em português.

### Testes

- Vitest + Testing Library para componentes e hooks; MSW simulando a API.
- Prioridade de cobertura: interceptor de refresh, guards de rota, fluxo de login.
- Um teste e2e de fumaça (Playwright) do login contra o backend real, a partir da Fase 1.

---

## 4. Fases

Cada fase é um PR (ou poucos) em `feature/<assunto>`, com Conventional Commits.

### Fase 0 — Fundação

- Scaffold Vite + React + TS estrito em `homehub-frontend/`; Tailwind 4 + shadcn/ui.
- ESLint (com regra de fronteiras de import), Prettier, Vitest, MSW.
- Proxy de `/api` no Vite; `httpClient` e `ApiError`.
- `.github/workflows/frontend-validation.yml`: lint, typecheck, testes, build, em PRs
  que tocam `homehub-frontend/**`.
- `.gitignore`, `homehub-frontend/README.md`, seção de frontend no AGENTS.md,
  atualização da stack no README e na spec.

**Pronto quando:** `npm run dev` abre uma página vazia com proxy funcionando e o CI passa.

### Fase 1 — Autenticação · *backend atual é suficiente*

- Tela de login (e-mail + senha, estados de erro conforme 2.6, loading, acessibilidade).
- `tokenStorage`, interceptor de Bearer, refresh com fila única (regra 7), tratamento
  401/403 conforme 2.3.
- Bootstrap de sessão via `/me`; logout chamando `/api/auth/logout`.
- Guards: `RequireAuth`, `RequireRole`, redirect de volta para a rota pretendida.
- Páginas 403 e 404.

**Pronto quando:** login, reload mantendo sessão, expiração do access token transparente
e logout funcionam contra o backend real, com testes cobrindo o interceptor e os guards.

### Fase 2 — Shell e dashboard

- `AppLayout`: sidebar, header com usuário e logout, tema claro/escuro, responsivo.
- Dashboard com grade de cards de serviço (nome, ícone, descrição, status, link).
- Dados vindos de MSW, seguindo o contrato de `GET /api/services` definido nesta fase.

**Pronto quando:** o portal é navegável de ponta a ponta com dados simulados.
**Entrega para o backend:** contrato de `Service` acordado (campos mínimos: `id`, `name`,
`description`, `url`, `icon`, `category`, `enabled`).

### Fase 3 — Serviços · *depende de `/api/services` e das decisões 2.1 e 2.2*

- Troca do mock pela API real; busca e filtro por categoria.
- Indicador de saúde com polling de `GET /api/services/{id}/health`.
- Admin: criar, editar e remover serviço.

### Fase 4 — Perfil · *depende de `/api/profile`*

- Ver e editar perfil; troca de senha com validação da política (regex do backend).

### Fase 5 — Administração de usuários · *depende de `/api/users`*

- Listagem, criação, edição, ativar/desativar, reset de senha. Rotas sob `RequireRole ADMIN`.

### Fase 6 — Sistema e segurança avançada · *depende de `/api/system` e 2FA*

- Saúde do sistema e notificações; fluxo de 2FA (login em duas etapas, ativação no perfil).

### Fase 7 — Empacotamento e deploy

- `Dockerfile` multi-stage gerando imagem `nginx:alpine` para `linux/arm64` (build no
  CI, não no Pi), com fallback de SPA e cache de assets.
- Entrada no `docker-compose.yml` e labels do Traefik.
- Migração do `tokenStorage` para cookies (2.4-C), se ainda não feita.

### Dependências entre front e backend

| Fase | Precisa do backend |
|---|---|
| 0, 1, 2 | nada novo (2.3 e 2.5 são desejáveis na Fase 1) |
| 3 | `/api/services` + cookie no Forward Auth (2.1) + decisão de roteamento (2.2) |
| 4 | `/api/profile` |
| 5 | `/api/users` |
| 6 | `/api/system`, 2FA |
| 7 | `docker-compose.yml`, Traefik |

As Fases 0–2 podem ser feitas em sequência agora. A partir da Fase 3 o frontend anda no
ritmo do backend; o MSW permite construir as telas antes, mas não fechá-las.

---

## 5. Decisões pendentes

1. **Sessão e Forward Auth (2.1 / 2.4):** migrar para cookies `HttpOnly`? Recomendado:
   sim, com `localStorage` como etapa provisória na Fase 1.
2. **Roteamento dos serviços (2.2):** sub-path ou subdomínio? Recomendado: subdomínio.
3. **Stack (2.8):** aprovar a atualização de versões e a inclusão de TanStack Query,
   React Hook Form, Zod e MSW.
4. **Correções no backend (2.3 e 2.5):** entram no mesmo trabalho do frontend ou em
   PRs separados do backend?
