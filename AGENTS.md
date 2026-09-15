# AGENTS.md

Instruções operacionais para agentes que trabalham neste repositório.

Descreve **o que existe no código hoje**. Onde o código diverge do `README.md` ou de
`docs/homehub-spec.md`, o código é a fonte de verdade e a divergência está na
seção [9. Pontos em aberto](#9-pontos-em-aberto--perguntar-antes-de-decidir).

Só existe um módulo com código: `homehub-backend/`. Não há `homehub-frontend/`,
`docker-compose.yml`, `Dockerfile` nem diretório de migrations, apesar de o README
citá-los como planejados.

---

## 1. Arquitetura — Ports & Adapters

Pacote raiz: `br.com.tmvinicius.home.hub`

```text
domain/
├── model/{user,auth}/          # entidades e value objects, sem anotação de framework
├── exception/{user,auth}/      # RuntimeException por regra de negócio
├── port/in/auth/               # contratos de entrada (casos de uso)
├── port/out/{auth,user}/       # contratos de saída (repositórios, provedores)
└── usecase/auth/               # *UseCaseImpl — POJOs, DI por construtor

infrastructure/
├── UseCaseConfig.java          # @Bean: portas in → *UseCaseImpl
├── persistence/
│   ├── PersistenceConfig.java  # @Bean: portas out → adapters + mappers
│   ├── repository/             # interfaces JpaRepository
│   ├── {user,auth}/            # entidades JPA (*Persistence)
│   ├── {user,auth}/adapter/    # implementam porta out
│   └── {user,auth}/mapper/     # domainToEntity / entityToDomain
├── security/
│   ├── SecurityConfig.java     # @Bean: TokenProvider, PasswordEncoder, JwtFilter, RefreshTokenGenerator
│   ├── WebSecurityConfig.java  # SecurityFilterChain e matchers de rota
│   ├── filter/ jwt/ password/
└── web/
    ├── controller/auth/  dto/request|response/  mapper/  exception/
```

Fluxo de uma requisição:

```text
Controller → AuthMapper → value object → porta in (UseCase)
          → *UseCaseImpl → porta out → *Adapter → *PersistenceMapper → JpaRepository
```

### Registro de beans — dois estilos deliberados

| Componente | Como é registrado |
|---|---|
| Use cases, adapters, mappers de persistência | `@Bean` explícito em `UseCaseConfig` / `PersistenceConfig` / `SecurityConfig`. As classes **não** têm `@Service`/`@Component`/`@Repository` |
| `AuthenticationController`, `AuthMapper`, `GlobalExceptionHandler` | anotação Spring (`@RestController`, `@Component`, `@ControllerAdvice`) |
| Repositórios JPA | interfaces `JpaRepository` |

Ao criar use case, adapter ou mapper de persistência, **registre o bean na
`@Configuration` correspondente e não anote a classe**.

Divisão das duas configs de segurança: `SecurityConfig` cria os beans de adapter;
`WebSecurityConfig` contém apenas o `SecurityFilterChain` e recebe `JwtFilter` por
construtor. Não misture as responsabilidades.

---

## 2. Regras obrigatórias

1. **O domínio não importa framework.** `domain/model`, `domain/port`, `domain/usecase`
   não podem importar `org.springframework.*`, `jakarta.persistence.*`,
   `io.jsonwebtoken.*` nem `lombok.*`.
2. **O domínio não importa `infrastructure`.** Existem duas violações herdadas (§9.5);
   não crie novas nem as use como precedente.
3. **DTO web não entra nem sai de caso de uso.** O caso de uso recebe value objects
   (`Email`, `Password`, `UUID`, `String`) e devolve modelo de domínio ou `record` de
   domínio (padrão: `LoginResult`). A conversão para DTO é feita no controller.
4. **Entidade JPA nunca chega ao domínio** — sempre passa por um `*PersistenceMapper`.
5. **Toda tecnologia fica atrás de uma porta**: JWT, BCrypt, `SecureRandom`, Spring Data.
6. **Dependências por construtor**, campos `private final`. Não use `@Autowired` em campo.
7. **Configuração via `@ConfigurationProperties` em `record`**, não `@Value`.
   `Application` já tem `@ConfigurationPropertiesScan`: um `*Properties` novo é detectado
   automaticamente, **não** adicione `@EnableConfigurationProperties`.
8. **Exceção de negócio nova** vai em `domain/exception/<contexto>/`, estende
   `RuntimeException` com construtor `(String message)`, e **precisa** de um
   `@ExceptionHandler` em `GlobalExceptionHandler` chamando `buildResponse(...)` com um
   `errorCode` em `SCREAMING_SNAKE_CASE`. Sem isso ela cai no
   `@ExceptionHandler(Exception.class)` e vira **500 INTERNAL_ERROR** silenciosamente.
9. **Rota nova exige matcher em `WebSecurityConfig`.** O fallback é
   `anyRequest().authenticated()`; sem matcher explícito, a rota fica autenticada e sem
   restrição de papel.
10. **Não altere o contrato de `/api/auth/verify`**: ele é o alvo do Forward Auth do
    Traefik (ver labels em `application.yml`).

---

## 3. Build e testes

Comandos a partir de `homehub-backend/`.

> `mvnw` está no índice do git como `100644`, **sem bit de execução**. Use `sh ./mvnw`.
> Não faça `chmod +x` sem pedido — isso altera o índice.

```bash
sh ./mvnw -B clean test        # compila + testes
sh ./mvnw -B clean verify      # o que o CI roda
sh ./mvnw spring-boot:run      # sobe em :8080
```

### Testcontainers exige Docker

`PersistenceIntegrationTest` sobe `postgres:16-alpine`. Sem daemon Docker ele falha com
`IllegalState Could not find a valid Docker environment` — **falha de ambiente, não do
código**.

Estado verificado nesta base: **57 testes no total; 56 passam e o único erro é esse.**
Sem Docker:

```bash
sh ./mvnw -B test -Dtest='!PersistenceIntegrationTest'   # verificado: 56 testes, BUILD SUCCESS
sh ./mvnw -B test -Dtest='LoginUseCaseImplTest'
sh ./mvnw -B test -Dtest='LoginUseCaseImplTest#shouldLoginValidUserWhenValidCredentials'
```

Rode no mínimo `sh ./mvnw -B clean test` antes de concluir, e diga explicitamente se
`PersistenceIntegrationTest` foi pulado por falta de Docker.

### Qualidade (etapas separadas no CI)

```bash
sh ./mvnw checkstyle:check                                   # failOnViolation=false
sh ./mvnw com.github.spotbugs:spotbugs-maven-plugin:check    # failThreshold=High
sh ./mvnw org.owasp:dependency-check-maven:check
```

Não há `checkstyle.xml` nem arquivo de exclusão do SpotBugs — valem os defaults dos plugins.
O CI (`.github/workflows/backend-validation.yml`) roda em PR para `main`/`develop` quando
`homehub-backend/**` muda.

### Armadilha do H2 (caminho relativo ao CWD)

`spring.datasource.url` é `jdbc:h2:file:./homehub-backend/data/homehub-db`, relativo ao
diretório do processo. Rodando Maven de dentro de `homehub-backend/`, a aplicação e
`ApplicationTests` (`@SpringBootTest`) criam um diretório aninhado
`homehub-backend/homehub-backend/data/`.

Depois de qualquer execução:

```bash
git status --short
rm -rf homehub-backend/homehub-backend     # a partir da raiz do repo
```

**Nunca comite** `homehub-backend/homehub-backend/` nem alteração em
`homehub-backend/data/*.db` (esses arquivos são rastreados — §9.2).

---

## 4. Spring Boot 4 — o que quebra a intuição de Boot 3

O projeto usa **Spring Boot 4.0.5**, com módulos e pacotes renomeados. Copiar código de
exemplos de Boot 3 falha na compilação.

| Em vez de | Aqui |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` | `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` |
| `...test.autoconfigure.web.servlet.AutoConfigureMockMvc` | `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` |
| `...test.autoconfigure.jdbc.AutoConfigureTestDatabase` | `org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase` |
| console H2 via starter web | dependência própria `spring-boot-h2console` |

`org.springframework.boot.test.context.SpringBootTest` continua no lugar antigo.
Os starters de teste também são modulares: `spring-boot-starter-data-jpa-test` e
`spring-boot-starter-webmvc-test` estão no `pom.xml` além do `spring-boot-starter-test`.

Testcontainers está na linha **2.0.4**, cujos artefatos são `testcontainers-junit-jupiter`
e `testcontainers-postgresql` (na 1.x seriam `junit-jupiter` e `postgresql`).

---

## 5. Como criar novos testes

### Convenções em vigor

- JUnit 5 + Mockito, com import estático coringa de
  `org.junit.jupiter.api.Assertions.*` e `org.mockito.Mockito.*`.
- AssertJ e Hamcrest **estão no classpath** (vêm do `spring-boot-starter-test`), mas
  **nenhum teste os usa**. Mantenha `Assertions.*`.
- Sufixo `Test` para tudo; integração usa `*IntegrationTest`. **Tudo roda no Surefire** —
  não há Failsafe nem profile separado.
- Nomes: `should<Comportamento>When<Condição>()`.
- Métodos de teste **sem `public`** (há o commit `style: remove public modifier from test
  methods`; sobrou um `public void setUp()` em `UserPersistenceAdapterTest`, que é a
  exceção, não o padrão).
- Fixtures em campos da classe, montadas em `@BeforeEach void setUp()`.
- Asserções relacionadas agrupadas em `assertAll(...)`.
- Verifique interação e não-interação: `verify(...)` e `verifyNoInteractions(...)`.
- `@DisplayName`, `@Nested` e `@ParameterizedTest` **não são usados** — não introduza sem
  necessidade clara.
- Não existe `src/test/resources`. Configuração vem de `@SpringBootTest(properties=…)`,
  `@DataJpaTest(properties=…)` ou `@DynamicPropertySource`.

### Padrão por tipo de alvo

| Alvo | Padrão de referência |
|---|---|
| Model / value object | JUnit puro, sem mocks — `EmailTest` |
| `*UseCaseImpl` | `@ExtendWith(MockitoExtension.class)` + `@Mock` nas portas + `@InjectMocks` — `LoginUseCaseImplTest` |
| Adapter de persistência | Mockito sobre `*JpaRepository` e o mapper — `UserPersistenceAdapterTest` |
| `JwtTokenAdapter` | instanciação direta com `JwtProperties` mockado — `JwtTokenAdapterTest` |
| `JwtFilter` | `MockHttpServletRequest/Response` + `FilterChain` mockado, limpando `SecurityContextHolder` em `@BeforeEach` **e** `@AfterEach` — `JwtFilterTest` |
| Vários use cases juntos | use cases **reais** montados no `setUp()`, só as portas mockadas — `AuthenticationFlowIntegrationTest` |
| Persistência real | `@Testcontainers` + `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + `@DynamicPropertySource` — `PersistenceIntegrationTest` |
| Cadeia de segurança | `@SpringBootTest(classes = …TestApplication.class)` + `@AutoConfigureMockMvc`, com `@SpringBootConfiguration` aninhada importando só `WebSecurityConfig` — `WebSecurityConfigIntegrationTest` |

`AuthenticationFlowIntegrationTest` é "integração" só no sentido de integrar use cases
entre si: **não sobe contexto Spring**. Siga esse padrão para fluxos de domínio e reserve
`@SpringBootTest` para quando o alvo do teste for a infraestrutura.

Para um container novo, reaproveite a configuração de `PersistenceIntegrationTest`:
container `static final`, `ddl-auto=create-drop`, `spring.flyway.enabled=false` e
limpeza com `deleteAll()` em `@BeforeEach`.

---

## 6. Convenções de código

- **Lombok só nas entidades JPA e em `RestError`** (`@Getter`, `@Setter`, `@Data`,
  `@Builder`, `@AllArgsConstructor`, `@NoArgsConstructor`). Nunca em `domain/`.
- `record` é usado para DTOs web, `*Properties` e `LoginResult`. Modelos de domínio são
  classes com getters manuais — **não os converta para `record`**.
- Validação de value object acontece **no construtor**, lançando exceção de domínio.
- Portas out retornam `Optional` nas buscas. O repositório JPA pode devolver entidade
  nula (`UserJpaRepository.findByEmail`) e o adapter envolve com `Optional.ofNullable`.
- Nomenclatura: `<Nome>Persistence` (tabela snake_case plural), `<Nome>JpaRepository`,
  `<Nome>Adapter` / `<Nome>PersistenceAdapter`, `<Nome>PersistenceMapper`
  (`domainToEntity` / `entityToDomain`), `<Verbo>UseCase` / `<Verbo>UseCaseImpl`,
  `<Nome>Request` / `<Nome>Response`, `<Nome>Properties`.
- Mensagens de exceção de negócio são escritas em português.

### Git

- Conventional Commits, assunto em inglês e minúsculo:
  `feat(auth): add logout endpoint`, `test: add JwtFilter unit test`.
  Tipos em uso: `feat`, `fix`, `test`, `docs`, `chore`, `build`, `refactor`, `style`.
- Branches de feature: `feature/<assunto>`, integradas por PR.
- Ao adicionar ou alterar endpoint, **atualize a coleção Bruno** em
  `homehub-backend/docs/api/bruno-collections/` (formato `opencollection 1.0.0`, um `.yml`
  por requisição com `seq`). É prática estabelecida no histórico.

---

## 7. Autenticação — estado atual

- **Access Token**: JWT HS256; claims `sub` (e-mail), `id` (UUID), `role`, `iat`, `exp`;
  expiração 24h (`jwt.expiration`).
- **Refresh Token**: opaco, 32 bytes de `SecureRandom` em Base64 URL sem padding,
  persistido em `refresh_tokens`, 7 dias. Não é JWT e **não há rotação** (ADR-008, decisão
  consciente): permanece válido até expirar ou ser revogado.
- Um usuário pode ter vários refresh tokens ativos (ADR-007). **Não** adicione unicidade
  em `user_id`.
- `JwtFilter` põe `AuthenticatedUser` como principal no `SecurityContext`. Token inválido
  **não lança**: o filtro segue sem autenticar e a cadeia decide o status.
- Controllers obtêm o usuário via `@AuthenticationPrincipal AuthenticatedUser`, nunca
  fazendo parsing de token.
- Rotas: `/api/auth/**` (login, register, verify, refresh, logout) e `/error` públicas;
  `/api/users/**` exige `ROLE_ADMIN`; `/api/services/**` e `/api/profile/**` exigem
  `ROLE_USER` ou `ROLE_ADMIN`; o resto exige autenticação. CSRF desabilitado, sessão
  `STATELESS`. O papel vira authority com prefixo `ROLE_`.

### Persistência

- H2 em arquivo no desenvolvimento; PostgreSQL só nos testes com Testcontainers.
- `ddl-auto: update` e **nenhuma migration**. Ao alterar uma entidade, lembre que o H2
  versionado em `data/` tem o schema antigo e `update` não remove nem renomeia colunas.
- **Não há nenhum `@Transactional` no código de produção.** `revokeToken` faz
  `findByToken` + `revoke()` + `save()` sem transação explícita.

---

## 8. Cuidados para não quebrar o que funciona

- **Não mexa nos regex de `Email` e `Password`.** A senha exige maiúscula, minúscula,
  dígito, símbolo de `@#$%^&+=!` e **6–15 caracteres**. Alterar invalida credenciais e
  vários testes.
- **`Password.fromHash(...)` é o único caminho para carregar um hash** — ele pula a
  validação. `new Password(hash)` valida e falha com hash BCrypt (60 caracteres).
- **Não existe endpoint de criação de usuário.** `/api/auth/register` está liberado no
  `WebSecurityConfig` mas não há controller; usuários são inseridos direto no banco. Não
  presuma um fluxo de cadastro ao montar um teste manual.
- **Não altere o `<argLine>` do `maven-surefire-plugin`**: ele carrega o javaagent do
  Mockito, necessário no JDK 21. Se precisar acrescentar argumentos, preserve o javaagent.
- **`JwtTokenAdapter` mistura duas gerações da API do JJWT**: a geração usa a API legada
  (`setSubject`, `setExpiration`, `SignatureAlgorithm`) e o parsing usa a de 0.12+
  (`Jwts.parser().verifyWith()`). Não migre só uma metade.
- `LoginUseCaseImpl` valida a senha **antes** de `user.validateUser()`; a ordem é o que os
  testes de usuário inativo exercitam.
- `User.setActive` só permite alteração quando o próprio `role` é `ADMIN` — regra incomum,
  com teste cobrindo. Não "corrija" sem pedido.
- `RefreshToken.validateRefreshToken()` checa revogação antes de expiração, e
  `revokeToken` a chama antes de revogar — então revogar duas vezes lança
  `RefreshTokenRevokedException`. Cada validação isolada tem teste; **o caso de revogação
  dupla não tem**, então mudanças aí não são pegas pela suíte.
- Existem **duas classes `RefreshTokenAdapter`** em pacotes diferentes (`security/jwt` é o
  gerador; `persistence/auth/adapter` é o repositório), e dois testes homônimos. Confira o
  import antes de editar.

---

## 9. Pontos em aberto — perguntar antes de decidir

Divergências entre código, documentação e intenção. **Não resolva por conta própria.**

1. **Documentação desatualizada.** `pom.xml`: Spring Boot **4.0.5**, JJWT **0.13.0**,
   Testcontainers 2.0.4, springdoc 3.0.2. README e spec dizem Boot 3.3.x, Security 6.x,
   JJWT 0.12.x.
2. **Banco de produção indefinido e arquivos versionados.** A spec diz PostgreSQL 16; o
   `application.yml` usa H2 em arquivo, sem profiles (`application-dev/prod` não existem).
   Além disso `data/homehub-db.mv.db` e `.trace.db` estão rastreados pelo git —
   provavelmente não intencional, mas removê-los é decisão do responsável.
3. **Migrations ausentes.** A spec prevê Flyway e `PersistenceIntegrationTest` já desliga
   `spring.flyway.enabled`, mas a dependência não está no `pom.xml`.
4. **JaCoCo não coleta nada.** O `<argLine>` literal do Surefire sobrescreve a propriedade
   definida por `jacoco:prepare-agent`; nenhum `jacoco.exec` é gerado e o relatório
   publicado pelo CI fica vazio. Corrigir exige mexer no `argLine`.
5. **Erro de login revela a política de senha.** `AuthMapper.toPassword` chama
   `Password.of(...)`, então uma senha fora do regex devolve **400 PASSWORD_INVALID** antes
   de qualquer verificação de credencial, enquanto credencial errada devolve 401. O mesmo
   vale para e-mail malformado (400 EMAIL_INVALID). Isso tensiona o princípio da spec
   §12.1 de não revelar informação no login.
6. **`BCryptPasswordAdapter.encode` provavelmente falha em runtime**: devolve
   `new Password(hashBCrypt)`, e o construtor valida contra o regex de senha crua (máx. 15
   caracteres). Não há chamador em produção nem teste cobrindo. A correção provável é usar
   `Password.fromHash(...)`.
7. **`AuthMapper.userLoginResponse(...)` não é usado** — o controller monta
   `UserLoginResponse` diretamente. Definir qual é o padrão.
8. **OpenAPI meio configurado.** `springdoc` está no `pom.xml` e `/v3/api-docs` e
   `/swagger-ui.html` sobem por padrão, mas nenhum controller tem anotação OpenAPI.
9. **Inconsistências menores de organização**: `UseCaseConfig` está em `infrastructure/` e
    não em `infrastructure/config/` como a spec descreve; `JwtTokenAdapterTest` e
    `RefreshTokenAdapterTest` (do gerador JWT) estão no pacote
    `infrastructure.security.password` mas testam classes de `...security.jwt`;
    `AuthenticationFlowIntegrationTest` está em `domain.integration.auth`. O prefixo real
    das properties de refresh token é `refresh-token`, não `security.refresh-token` como
    sugere a spec.
