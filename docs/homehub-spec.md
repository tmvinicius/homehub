# HomeHub — Especificação e Evolução do Projeto

> Documento vivo de referência técnica, arquitetura, estado atual e histórico de funcionalidades do HomeHub.

| Campo | Valor |
|---|---|
| **Versão do documento** | 2.0.0 |
| **Última atualização** | 2026-07-30 |
| **Responsável** | vinicius-mascarenhas |
| **Status do projeto** | V1 — Backend de autenticação em evolução |
| **Linguagem do backend** | Java 21 |
| **Linguagem do frontend** | TypeScript |
| **Ambiente-alvo** | Raspberry Pi 4 / ARM64 |

---

## 1. Como usar este documento

Este arquivo possui quatro funções:

1. Descrever a visão e as restrições do HomeHub.
2. Registrar a arquitetura e os padrões adotados.
3. Indicar claramente o que está implementado, planejado ou fora de escopo.
4. Documentar a evolução de cada feature sem reescrever toda a especificação.

Ao adicionar uma nova funcionalidade:

1. Atualize a matriz de status.
2. Atualize o contrato de API, quando aplicável.
3. Registre decisões arquiteturais relevantes.
4. Adicione uma entrada no histórico de features.
5. Atualize os próximos passos.

Um modelo pronto para novas features está disponível no final deste documento.

---

## 2. Visão geral

### 2.1 O que é o HomeHub

O HomeHub é um portal web self-hosted que centraliza o acesso a serviços domésticos containerizados por meio de uma autenticação única.

A aplicação funciona como uma homepage privada a partir da qual usuários autenticados podem acessar serviços como:

- download de vídeos;
- media server;
- cliente torrent;
- backup de fotos;
- workspace de notas;
- monitoramento de infraestrutura.

### 2.2 Propósito

- Servir como projeto de portfólio com qualidade profissional.
- Ser uma plataforma utilitária real para uso doméstico.
- Permitir inclusão modular de novos serviços.
- Demonstrar arquitetura limpa, segurança, observabilidade e containerização.

### 2.3 Escopo inicial

A primeira versão concentra-se em:

- autenticação e autorização;
- gerenciamento de usuários;
- registro e acesso a serviços;
- integração com reverse proxy;
- execução em ambiente ARM64;
- infraestrutura via Docker Compose.

---

## 3. Restrições e requisitos não funcionais

### 3.1 Ambiente-alvo

| Restrição | Valor |
|---|---|
| Hardware | Raspberry Pi 4 com 4 GB ou 8 GB de RAM |
| Armazenamento | SSD externo USB 3.0, 256 GB ou superior |
| Sistema operacional | Raspberry Pi OS Lite 64-bit ou Ubuntu Server ARM64 |
| Usuários simultâneos | Até 5 |
| Acesso inicial | Rede local |
| Evolução de acesso | Cloudflare Tunnel ou solução equivalente |
| Arquitetura de CPU | ARM64 / aarch64 |
| Containerização | Docker Compose |

### 3.2 Requisitos de qualidade

- Consumo total desejado abaixo de aproximadamente 3,5 GB de RAM.
- Tempo de resposta inferior a 500 ms para páginas e operações simples.
- Inicialização completa dos containers em até 3 minutos.
- Nenhuma dependência obrigatória no host além do Docker.
- Backend desenvolvido com Java 21.
- Frontend desenvolvido com TypeScript em modo estrito.
- Imagens Docker compatíveis com ARM64.
- Código organizado segundo Ports & Adapters.

---

## 4. Arquitetura geral

### 4.1 Visão de infraestrutura

```text
Cliente
  ↓
Traefik
  ├── /api/*        → Backend Spring Boot
  ├── /metube/*     → MeTube
  ├── /jellyfin/*   → Jellyfin
  ├── /torrents/*   → qBittorrent
  ├── /photos/*     → Immich
  ├── /notes/*      → AFFiNE
  ├── /grafana/*    → Grafana
  ├── /status/*     → Uptime Kuma
  └── /*            → Frontend React
```

O Traefik será responsável por:

- reverse proxy;
- roteamento por path;
- descoberta de serviços por labels Docker;
- terminação HTTPS;
- integração futura com Forward Auth.

### 4.2 Componentes principais

| Componente | Responsabilidade |
|---|---|
| Frontend React | Interface do portal |
| Backend Spring Boot | Autenticação, autorização e API |
| PostgreSQL | Persistência principal |
| Redis | Cache, rate limiting e revogação futura |
| Traefik | Reverse proxy e entrada da plataforma |
| Prometheus | Coleta de métricas |
| Grafana | Visualização de métricas |
| Uptime Kuma | Monitoramento de disponibilidade |
| Watchtower | Atualização de imagens, sujeita a revisão para produção |

---

## 5. Arquitetura interna do backend

### 5.1 Estilo arquitetural

O backend utiliza Clean Architecture pragmática com Ports & Adapters.

```text
domain/
├── model/
├── port/
│   ├── in/
│   └── out/
└── usecase/

infrastructure/
├── web/
├── persistence/
├── security/
├── config/
├── cache/
└── external/
```

### 5.2 Responsabilidades por camada

#### Domínio

Contém:

- entidades;
- value objects;
- resultados de casos de uso;
- contratos de entrada;
- contratos de saída;
- regras e orquestrações de negócio.

O domínio não deve importar Spring, JPA, Redis, bibliotecas JWT ou detalhes HTTP.

#### Infraestrutura web

Contém:

- controllers;
- DTOs de request e response;
- mappers web;
- tratamento global de exceções.

#### Infraestrutura de persistência

Contém:

- entidades JPA;
- repositórios Spring Data;
- mappers de persistência;
- adapters que implementam portas do domínio.

#### Infraestrutura de segurança

Contém:

- geração e validação de JWT;
- geração de Refresh Token;
- BCrypt;
- filtros Spring Security;
- configuração da cadeia de segurança.

#### Configuração

Responsável por montar a aplicação:

- registrar beans;
- ligar portas a adapters;
- carregar propriedades tipadas;
- evitar dependência do domínio com o framework.

### 5.3 Regras arquiteturais

1. O domínio não depende da infraestrutura.
2. Controllers não implementam regras de negócio.
3. DTOs web não são retornados por casos de uso.
4. Implementações tecnológicas ficam atrás de portas.
5. Configurações externas usam `@ConfigurationProperties`.
6. Casos de uso recebem dependências por construtor.
7. Objetos de domínio devem ser preferencialmente imutáveis.

---

## 6. Stack tecnológica

### 6.1 Backend

| Tecnologia | Versão ou linha adotada | Finalidade |
|---|---|---|
| Java | 21 LTS | Linguagem |
| Spring Boot | 3.3.x | Framework principal |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data JPA | Compatível com Spring Boot | Persistência |
| JJWT | 0.12.x | JWT |
| BCrypt | Spring Security | Hash de senha |
| Maven | 3.9.x | Build |
| Flyway | Compatível com o projeto | Migrações |
| Springdoc OpenAPI | 2.x | Documentação futura da API |
| SpotBugs | Maven Plugin | Análise estática |

### 6.2 Frontend

| Tecnologia | Linha planejada | Finalidade |
|---|---|---|
| React | 18.x | Interface |
| TypeScript | 5.x | Tipagem |
| Vite | 5.x | Build |
| Tailwind CSS | 4.x | Estilos |
| shadcn/ui | Atual | Componentes |
| React Router | 6.x | Rotas |
| Zustand | 5.x | Estado |
| Axios | 1.x | Cliente HTTP |
| Vitest | Atual | Testes |

### 6.3 Infraestrutura

| Tecnologia | Finalidade |
|---|---|
| Docker / Docker Compose | Containerização |
| Traefik 3 | Reverse proxy |
| PostgreSQL 16 | Banco principal |
| Redis 7 | Cache e segurança futura |
| Prometheus | Métricas |
| Grafana | Dashboards |
| Uptime Kuma | Uptime |
| Watchtower | Atualização automatizada |

---

## 7. Estado atual do projeto

### 7.1 Matriz de funcionalidades

| Funcionalidade | Status | Observação |
|---|---|---|
| Login com e-mail e senha | Implementado | Usa value objects e caso de uso |
| Validação BCrypt | Implementado | Adapter de infraestrutura |
| Geração de Access Token JWT | Implementado | Claims tipadas |
| Validação de JWT | Implementado | Assinatura e expiração |
| Filtro JWT | Implementado | Integração com Spring Security |
| SecurityContext | Implementado | Principal tipado |
| Endpoint `/api/auth/verify` | Implementado | Preparação para Forward Auth |
| Endpoint `/api/auth/me` | Implementado | Usa `@AuthenticationPrincipal` |
| Refresh Token — modelo | Implementado | Entidade de domínio |
| Refresh Token — geração segura | Implementado | Valor opaco com `SecureRandom` |
| Refresh Token — persistência | Implementado | Port, adapter, mapper e JPA |
| Refresh Token no login | Implementado | Retorna junto ao Access Token |
| Endpoint `/api/auth/refresh` | Planejado | Renovação ainda não concluída |
| Logout e revogação | Planejado | Estratégia a definir |
| Rotação de Refresh Token | Planejado | Recomendado para evolução |
| 2FA com TOTP | Planejado | Fora do escopo atual |
| Rate limiting | Planejado | Redis é candidato |
| Forward Auth completo | Em preparação | Endpoint de verificação existe |
| Testes automatizados | Parcial | Ampliar cobertura |
| Observabilidade | Planejado | Prometheus e Grafana |
| Deploy em Raspberry Pi | Planejado | Após estabilização da V1 |

---

## 8. Autenticação e autorização

### 8.1 Fluxo atual de login

```text
POST /api/auth/login
        ↓
Email e senha
        ↓
LoginUseCase
        ↓
Busca do usuário
        ↓
Validação da senha
        ↓
Validação do estado do usuário
        ↓
Geração do Access Token
        ↓
Criação e persistência do Refresh Token
        ↓
LoginResult
        ↓
UserLoginResponse
```

### 8.2 Access Token

O Access Token é um JWT de curta duração.

Claims utilizadas atualmente:

| Claim | Conteúdo |
|---|---|
| `sub` | E-mail do usuário |
| `id` | UUID do usuário |
| `role` | Papel do usuário |
| `iat` | Data de emissão |
| `exp` | Data de expiração |

Responsabilidades do `TokenProvider`:

- gerar JWT;
- validar token;
- extrair claims;
- converter o token em `AuthenticatedUser`.

### 8.3 Refresh Token

O Refresh Token atual é um valor opaco, aleatório e persistido.

Ele não é um JWT.

#### Modelo

O modelo contém:

- identificador interno;
- identificador do usuário;
- valor público do token;
- data de expiração;
- indicador de revogação.

#### Fluxo de criação

```text
LoginUseCaseImpl
        ↓
RefreshTokenUseCase
        ↓
RefreshTokenGenerator
        ↓
RefreshTokenRepository
        ↓
Banco de dados
```

#### Decisões atuais

- O `userId` é fornecido pelo fluxo de login.
- Não é realizada nova busca por e-mail para gerar o token.
- O valor é produzido com `SecureRandom`.
- Um usuário pode possuir múltiplos Refresh Tokens.
- O DTO HTTP não expõe o objeto completo de domínio.
- O tempo de expiração é configurado externamente.

### 8.4 Resposta do login

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "opaque-refresh-token"
}
```

### 8.5 Fluxo futuro de renovação

Endpoint planejado:

```text
POST /api/auth/refresh
```

Fluxo esperado:

1. Receber o Refresh Token.
2. Localizar o token persistido.
3. Validar existência.
4. Validar expiração.
5. Validar revogação.
6. Recuperar o usuário associado.
7. Gerar novo Access Token.
8. Avaliar rotação do Refresh Token.
9. Retornar os novos valores.

### 8.6 Integração com Spring Security

```text
Request
  ↓
JwtFilter
  ↓
TokenProvider.parseAndValidate()
  ↓
AuthenticatedUser
  ↓
UsernamePasswordAuthenticationToken
  ↓
SecurityContextHolder
  ↓
Controller
```

O endpoint `/api/auth/me` usa `@AuthenticationPrincipal`, evitando parsing manual de JWT no controller.

### 8.7 Roles

| Role | Permissões gerais |
|---|---|
| USER | Acesso aos serviços autorizados e ao próprio perfil |
| ADMIN | Administração de usuários, serviços e configurações |

---

## 9. Contratos atuais da API

### 9.1 Autenticação

| Método | Endpoint | Status | Autenticação | Descrição |
|---|---|---|---|---|
| POST | `/api/auth/login` | Implementado | Pública | Autentica e retorna Access + Refresh Token |
| GET | `/api/auth/verify` | Implementado | Bearer Token | Valida o token atual |
| GET | `/api/auth/me` | Implementado | Bearer Token | Retorna o usuário autenticado |
| POST | `/api/auth/refresh` | Planejado | Refresh Token | Renova o Access Token |
| POST | `/api/auth/logout` | Planejado | Autenticada | Revoga a sessão |
| POST | `/api/auth/verify-2fa` | Planejado | Temporária | Valida TOTP |

### 9.2 Usuários

| Método | Endpoint | Status | Acesso |
|---|---|---|---|
| GET | `/api/users` | Planejado | ADMIN |
| GET | `/api/users/{id}` | Planejado | ADMIN |
| POST | `/api/users` | Planejado | ADMIN |
| PUT | `/api/users/{id}` | Planejado | ADMIN |
| PATCH | `/api/users/{id}/toggle-active` | Planejado | ADMIN |
| PATCH | `/api/users/{id}/reset-password` | Planejado | ADMIN |

### 9.3 Perfil

| Método | Endpoint | Status | Acesso |
|---|---|---|---|
| GET | `/api/profile` | Planejado | USER |
| PUT | `/api/profile` | Planejado | USER |
| PUT | `/api/profile/password` | Planejado | USER |
| POST | `/api/profile/2fa/enable` | Planejado | USER |
| POST | `/api/profile/2fa/confirm` | Planejado | USER |
| POST | `/api/profile/2fa/disable` | Planejado | USER |

### 9.4 Serviços

| Método | Endpoint | Status | Acesso |
|---|---|---|---|
| GET | `/api/services` | Planejado | USER |
| GET | `/api/services/{id}` | Planejado | USER |
| GET | `/api/services/{id}/health` | Planejado | USER |
| POST | `/api/services` | Planejado | ADMIN |
| PUT | `/api/services/{id}` | Planejado | ADMIN |
| DELETE | `/api/services/{id}` | Planejado | ADMIN |

### 9.5 Sistema

| Método | Endpoint | Status | Acesso |
|---|---|---|---|
| GET | `/api/system/health` | Planejado | USER |
| GET | `/api/system/notifications` | Planejado | USER |

---

## 10. Configuração e propriedades

### 10.1 Padrão adotado

Configurações da aplicação devem usar:

```java
@ConfigurationProperties
```

Preferencialmente com records imutáveis.

Classes de configuração Spring são responsáveis por criar os beans e ligar implementações às portas.

Evitar:

```java
@Value
```

espalhado em adapters e casos de uso.

### 10.2 BCrypt

Exemplo:

```yaml
security:
  password:
    bcrypt:
      strength: 10
```

### 10.3 Refresh Token

Exemplo conceitual:

```yaml
security:
  refresh-token:
    expiration: 7d
```

A propriedade deve ser registrada por `@ConfigurationPropertiesScan` ou `@EnableConfigurationProperties`.

---

## 11. Persistência

### 11.1 Princípios

- Entidades JPA permanecem na infraestrutura.
- Modelos de domínio não recebem anotações JPA.
- Mappers convertem domínio e persistência.
- Repositórios Spring Data não são expostos ao domínio.
- Adapters implementam as portas de saída.

### 11.2 Refresh Tokens

A persistência de Refresh Token suporta múltiplas sessões por usuário.

Não deve existir unicidade obrigatória em `user_id`.

Índices recomendados:

- índice único para o valor do token;
- índice para `user_id`;
- índice para `expires_at`, caso exista limpeza programada.

---

## 12. Segurança

### 12.1 Princípios atuais

- Senhas armazenadas com BCrypt.
- JWT assinado e com expiração.
- Refresh Tokens gerados de forma aleatória.
- Controllers desacoplados da biblioteca JWT.
- Usuário autenticado recuperado do `SecurityContext`.
- Credenciais inválidas não devem revelar se o e-mail existe.
- Segredos e chaves não devem ser versionados.

### 12.2 Melhorias planejadas

- armazenamento seguro do Refresh Token no cliente;
- rotação de Refresh Token;
- revogação de sessão;
- logout global;
- rate limiting;
- proteção contra reutilização de token;
- auditoria de sessões;
- hardening de cookies;
- HTTPS obrigatório fora do ambiente local.

---

## 13. Qualidade e CI

### 13.1 Validações

O workflow de backend deve incluir progressivamente:

- compilação;
- testes;
- SpotBugs;
- análise de dependências;
- verificação de estilo;
- build da imagem Docker.

### 13.2 SpotBugs

Alertas devem ser classificados entre:

- bug real;
- risco arquitetural;
- falso positivo;
- alerta suprimido com justificativa.

Não remover validações de domínio apenas para satisfazer análise estática.

Supressões devem ser:

- específicas;
- documentadas;
- justificadas;
- revisáveis.

---

## 14. Decisões arquiteturais registradas

### ADR-001 — Ports & Adapters

**Status:** Aceita

O domínio define contratos e a infraestrutura fornece implementações.

### ADR-002 — Configuração tipada

**Status:** Aceita

Configurações usam `@ConfigurationProperties`, evitando `@Value` disperso.

### ADR-003 — JWT como detalhe de infraestrutura

**Status:** Aceita

O domínio trabalha com `AuthenticatedUser`, não com claims ou tipos da biblioteca JWT.

### ADR-004 — SecurityContext como fonte do usuário autenticado

**Status:** Aceita

Controllers usam `@AuthenticationPrincipal` em vez de interpretar tokens.

### ADR-005 — Refresh Token opaco e persistido

**Status:** Aceita

O Refresh Token não é JWT. Ele é aleatório, revogável e persistido.

### ADR-006 — LoginResult independente da web

**Status:** Aceita

O caso de uso retorna um resultado da camada de domínio/aplicação. O controller converte esse resultado para o DTO HTTP.

### ADR-007 — Múltiplas sessões por usuário

**Status:** Aceita

Um usuário pode possuir vários Refresh Tokens válidos, permitindo múltiplos dispositivos e navegadores.

---

## 15. Histórico de features

### 15.1 Fundação da autenticação JWT

**Status:** Concluída

#### Objetivo

Criar o fluxo inicial de autenticação baseado em e-mail, senha e Access Token JWT.

#### Entregas

- `Email` e `Password` como value objects;
- `LoginUseCase`;
- `PasswordEncoder`;
- `TokenProvider`;
- BCrypt;
- geração de JWT;
- propriedades externas de segurança.

#### Impacto arquitetural

Estabeleceu Ports & Adapters para autenticação.

---

### 15.2 Validação JWT e integração com Spring Security

**Status:** Concluída

#### Objetivo

Centralizar autenticação na cadeia de filtros do Spring Security.

#### Entregas

- `JwtFilter`;
- `AuthenticatedUser`;
- `VerifyTokenUseCase`;
- endpoint `/api/auth/verify`;
- `SecurityContextHolder`;
- endpoint `/api/auth/me`;
- uso de `@AuthenticationPrincipal`;
- renomeação para `AuthenticationController`.

#### Impacto arquitetural

Controllers deixaram de interpretar JWT diretamente.

---

### 15.3 Refresh Token

**Status:** Parcialmente concluída

#### Objetivo

Adicionar sessões renováveis ao fluxo de autenticação.

#### Entregas concluídas

- modelo de domínio `RefreshToken`;
- porta `RefreshTokenGenerator`;
- porta `RefreshTokenRepository`;
- `RefreshTokenUseCase`;
- geração segura com `SecureRandom`;
- propriedades de expiração;
- entidade JPA;
- repositório JPA;
- mapper de persistência;
- adapter de persistência;
- integração com `LoginUseCaseImpl`;
- `LoginResult`;
- retorno de Access Token e Refresh Token no login;
- registro dos beans necessários;
- atualização de documentação e collections Bruno.

#### Pendências

- endpoint `/api/auth/refresh`;
- validação de token expirado ou revogado;
- rotação;
- logout;
- revogação;
- limpeza de tokens expirados;
- testes automatizados específicos.

#### Impacto arquitetural

O login passou a coordenar a geração de Access Token e a criação de uma sessão persistente sem depender de DTOs web.

---

## 16. Próximos passos

### Prioridade imediata

1. Implementar consulta de Refresh Token por valor.
2. Implementar validação de expiração e revogação.
3. Criar `POST /api/auth/refresh`.
4. Adicionar testes unitários e de integração.
5. Definir estratégia de rotação.

### Próxima etapa

1. Logout e revogação.
2. Limpeza de tokens expirados.
3. Migrações Flyway.
4. PostgreSQL em ambiente local.
5. Tratamento global de exceções.
6. OpenAPI.
7. Observabilidade.

### Evolução futura

- 2FA;
- rate limiting;
- Forward Auth completo;
- frontend;
- integração dos serviços;
- deploy ARM64;
- acesso remoto seguro.

---

## 17. Modelo para registrar nova feature

Copie a seção abaixo para o final de **Histórico de features**.

```markdown
### X.Y Nome da feature

**Status:** Planejada | Em desenvolvimento | Parcialmente concluída | Concluída

#### Contexto

Descreva o problema ou necessidade que motivou a feature.

#### Objetivo

Descreva o resultado esperado.

#### Escopo

- item incluído;
- item incluído;
- item explicitamente fora do escopo.

#### Entregas

- modelos de domínio;
- portas;
- casos de uso;
- adapters;
- persistência;
- endpoints;
- documentação;
- testes.

#### Fluxo

```text
Entrada
  ↓
Caso de uso
  ↓
Porta
  ↓
Adapter
  ↓
Saída
```

#### Contrato de API

| Método | Endpoint | Request | Response | Autenticação |
|---|---|---|---|---|
| POST | `/api/...` | `{ ... }` | `{ ... }` | Pública/USER/ADMIN |

#### Decisões arquiteturais

- decisão;
- motivação;
- alternativas descartadas.

#### Configurações

```yaml
feature:
  property: value
```

#### Persistência

- tabela;
- índices;
- relacionamentos;
- estratégia de migração.

#### Segurança

- ameaças consideradas;
- validações;
- autorização;
- dados sensíveis.

#### Testes

- unitários;
- integração;
- casos de erro;
- testes manuais.

#### Pendências

- item futuro;
- dívida técnica;
- melhoria não bloqueante.

#### Commits relacionados

```text
feat(...): ...
fix(...): ...
docs(...): ...
```

#### Impacto arquitetural

Descreva como a feature altera responsabilidades, dependências ou fluxos.
```

---

## 18. Convenções de manutenção

- Atualizar a data e a versão do documento após mudanças relevantes.
- Não misturar estado implementado com visão futura.
- Usar os status: `Planejado`, `Em desenvolvimento`, `Parcialmente concluído`, `Implementado` e `Descontinuado`.
- Registrar apenas decisões relevantes e duradouras.
- Evitar duplicar a mesma explicação em várias seções.
- Atualizar contratos quando endpoints mudarem.
- Registrar pendências sem apresentá-las como funcionalidades concluídas.
- Manter exemplos alinhados ao comportamento real da aplicação.
