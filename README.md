# HomeHub

HomeHub é um portal web self-hosted pensado para centralizar, em uma única interface, o acesso a serviços domésticos containerizados.

A proposta é transformar diferentes aplicações do ambiente doméstico em uma experiência única de acesso, autenticação e navegação.

---

## 🏠 O que é o HomeHub

O HomeHub funciona como uma homepage privada para serviços self-hosted.

A ideia é que um usuário autenticado possa acessar aplicações como:

- media server;
- download de vídeos;
- cliente torrent;
- backup de fotos;
- workspace de notas;
- dashboards;
- monitoramento de infraestrutura.

O projeto também serve como laboratório prático para aplicar conceitos de arquitetura, segurança, testes, containerização e observabilidade.

---

## 🎯 Motivação

Ambientes self-hosted costumam crescer de maneira fragmentada.

Cada serviço normalmente possui:

- sua própria porta;
- seu próprio endereço;
- sua própria interface;
- sua própria autenticação;
- sua própria forma de administração.

O HomeHub nasce para reduzir essa fragmentação e criar um ponto central de entrada para o ambiente doméstico.

Os principais objetivos são:

- centralizar serviços;
- oferecer autenticação e autorização únicas;
- facilitar a inclusão de novos serviços;
- manter uma arquitetura modular;
- permitir execução em hardware ARM64;
- aplicar boas práticas de engenharia de software;
- servir como projeto de portfólio e uso real.

---

## 🧩 Visão geral da arquitetura

O projeto utiliza uma arquitetura dividida entre frontend, backend e infraestrutura.

```text
Cliente
  ↓
Traefik
  ├── /api/*        → HomeHub Backend
  ├── /jellyfin/*   → Jellyfin
  ├── /metube/*     → MeTube
  ├── /torrents/*   → qBittorrent
  ├── /photos/*     → Immich
  ├── /notes/*      → AFFiNE
  ├── /grafana/*    → Grafana
  ├── /status/*     → Uptime Kuma
  └── /*            → HomeHub Frontend
```

O backend segue uma abordagem de **Clean Architecture com Ports & Adapters**, mantendo o domínio desacoplado de frameworks e detalhes de infraestrutura.

---

## 🚀 Estado atual

O desenvolvimento está atualmente concentrado no backend.

Já existem, entre outras partes:

- login com e-mail e senha;
- validação BCrypt;
- Access Token JWT;
- Spring Security;
- autenticação via `SecurityContext`;
- Refresh Token opaco e persistido;
- renovação de Access Token;
- logout por revogação do Refresh Token;
- testes unitários;
- testes automatizados;
- testes de integração para partes críticas.

O frontend ainda está planejado e será iniciado após a estabilização da base principal do backend.

---

## 🛠️ Stack resumida

### Backend

- Java 21
- Spring Boot 3.3.x
- Spring Security 6.x
- Spring Data JPA
- PostgreSQL 16
- JJWT 0.12.x
- BCrypt
- Maven 3.9.x
- JUnit 5
- Mockito
- Testcontainers
- SpotBugs
- Bruno

### Frontend planejado

- React 18
- TypeScript 5
- Vite 5
- Tailwind CSS 4
- shadcn/ui
- React Router 6
- Zustand 5
- Axios 1
- Vitest

### Infraestrutura planejada

- Docker
- Docker Compose
- Traefik 3
- Redis 7
- Prometheus
- Grafana
- Uptime Kuma
- Cloudflare Tunnel ou solução equivalente

---

## 🗺️ Roadmap

### Backend

- [x] Base de autenticação
- [x] Refresh Token
- [x] Logout por revogação
- [x] Testes principais de autenticação
- [ ] Migrações com Flyway
- [ ] Tratamento global de exceções
- [ ] OpenAPI
- [ ] Gerenciamento de usuários
- [ ] Perfil do usuário
- [ ] Cadastro e consulta de serviços
- [ ] Observabilidade
- [ ] Hardening de segurança
- [ ] Preparação ARM64

### Frontend

- [ ] Estrutura inicial React + TypeScript
- [ ] Autenticação
- [ ] Dashboard principal
- [ ] Gerenciamento de sessão
- [ ] Navegação por serviços
- [ ] Área de perfil
- [ ] Área administrativa

### Infraestrutura

- [ ] Dockerização completa
- [ ] Docker Compose
- [ ] Traefik
- [ ] PostgreSQL
- [ ] Monitoramento
- [ ] Reverse proxy
- [ ] Acesso remoto seguro

---

## 📦 Estrutura do monorepo

```text
homehub/
├── README.md
│
├── homehub-backend/
│   ├── README.md
│   ├── docs/
│   ├── src/
│   └── pom.xml
│
├── homehub-frontend/
│   ├── README.md
│   ├── src/
│   └── package.json
│
└── docker-compose.yml
```

A ideia é manter frontend e backend no mesmo repositório, mas com documentação e ciclos de desenvolvimento bem definidos para cada módulo.

---

## 📚 Documentação por módulo

Para detalhes técnicos, execução local e dependências:

- [Backend](./homehub-backend/README.md)
- [Frontend](./homehub-frontend/README.md)

---

## Status

> Projeto em desenvolvimento.

A versão `1.0.0` será considerada estável quando autenticação, frontend, gerenciamento dos serviços e infraestrutura principal estiverem integrados de forma consistente.
