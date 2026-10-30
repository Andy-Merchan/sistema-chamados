# Sistema de Chamados IFRN

API REST para registrar e acompanhar chamados de suporte do IFRN, desenvolvida para a disciplina **Desenvolvimento de Sistemas Corporativos**.

Hoje, quando um projetor para de funcionar ou o Wi-Fi do laboratório cai, o aviso é feito por WhatsApp, e-mail ou pessoalmente, sem registro nem histórico. Com esta API, o **solicitante** abre um chamado informando o problema, o local e a categoria; o chamado vai para o **setor** responsável (ex.: COTIC para TI, Manutenção para infraestrutura), onde um **técnico** o assume, registra o andamento e o encerra.

## Tecnologias

- Java 21 e Spring Boot 4.1 (Maven)
- Spring Web MVC, Spring Data JPA e Bean Validation
- PostgreSQL 17 e Flyway (migrations)
- Lombok
- springdoc-openapi (documentação OpenAPI e Swagger UI)
- Docker e Docker Compose

## Como executar

Pré-requisito: apenas o [Docker](https://www.docker.com/products/docker-desktop/) instalado e em execução.

```bash
git clone https://github.com/Andy-Merchan/sistema-chamados.git
cd sistema-chamados
docker compose up --build
```

A API fica disponível em **http://localhost:8080**. Na primeira execução, o Flyway cria as tabelas e insere os dados iniciais automaticamente.

| Comando | O que faz |
|---------|-----------|
| `docker compose up -d` | sobe os contêineres em segundo plano |
| `docker compose logs -f api` | acompanha os logs da API |
| `docker compose down` | para os contêineres (os dados continuam no volume) |
| `docker compose down -v` | para e **apaga** o volume (banco volta ao estado inicial) |

**Credenciais do banco:** o `docker-compose.yml` lê `DB_USERNAME` e `DB_PASSWORD` de um arquivo `.env` opcional na raiz do projeto (que não é versionado). Sem esse arquivo, usa valores padrão de desenvolvimento, então nenhum passo manual é necessário. O PostgreSQL só aplica a senha na criação do volume: se ela for alterada depois, é preciso rodar `docker compose down -v`.

O que cada arquivo faz:

- **`Dockerfile`** (multi-stage): a primeira etapa compila o projeto com Maven e JDK 21 (`-DskipTests`, pois o teste `contextLoads` precisa de banco); a segunda copia apenas o `.jar` para uma imagem com JRE 21, menor e sem código-fonte.
- **`docker-compose.yml`**: sobe o PostgreSQL (com volume `chamados-dados` e *healthcheck*) e a API, que só inicia quando o banco está pronto. A API acessa o banco pela rede interna do Compose (`db:5432`); a porta do banco não é publicada no host.
- **`.dockerignore`**: evita enviar `target/`, `.git/`, `.idea/` e `.env` para o build.

## Arquitetura

O código fica no pacote `br.edu.ifrn.sistemachamados`, separado em camadas:

| Pacote | Responsabilidade |
|--------|------------------|
| `controller` | recebe a requisição HTTP, chama o Service e devolve a resposta; **sem regras de negócio** |
| `service` | concentra as regras de negócio e lança exceções próprias |
| `repository` | interfaces que estendem `JpaRepository`; filtros com Specifications |
| `model` | entidades JPA (`Setor`, `Categoria`, `Usuario`, `Chamado`, `Interacao`) e enums |
| `dto` | objetos de entrada (`*Request`, `*Filtro`) e saída (`*Response`), para não expor as entidades |
| `exception` | `RegraNegocioException`, `RecursoNaoEncontradoException` e o `@RestControllerAdvice` |

A injeção de dependências é feita **por construtor**, e os enums são gravados como texto (`@Enumerated(EnumType.STRING)`).

### Modelo

| Entidade | Relacionamentos |
|----------|-----------------|
| Setor | tem várias categorias e vários técnicos |
| Categoria | pertence a um setor |
| Usuario | `SOLICITANTE` ou `TECNICO`; o técnico pertence a um setor |
| Chamado | tem um solicitante, uma categoria e, opcionalmente, um técnico |
| Interacao | pertence a um chamado e tem um autor |

- **Prioridade:** `BAIXA`, `MEDIA`, `ALTA`
- **Status:** `ABERTO`, `EM_ATENDIMENTO`, `RESOLVIDO`, `CANCELADO`

### Ciclo de vida do chamado

```
ABERTO ──assumir──> EM_ATENDIMENTO ──resolver──> RESOLVIDO
   │
   └──cancelar──> CANCELADO
```

Nenhuma outra transição é permitida.

### Regras de negócio (camada Service)

1. Todo chamado nasce `ABERTO`, com `dataAbertura` preenchida pelo sistema e prioridade `MEDIA` quando não informada.
2. Apenas usuários `SOLICITANTE` podem abrir chamados.
3. Um técnico só pode assumir um chamado `ABERTO` cuja categoria pertença ao seu setor; o status passa a `EM_ATENDIMENTO`.
4. Somente o técnico atribuído pode resolver, e a solução é obrigatória; `dataFechamento` é preenchida.
5. Somente o próprio solicitante pode cancelar, e apenas enquanto o chamado estiver `ABERTO`.
6. Chamados `RESOLVIDO` ou `CANCELADO` não aceitam alterações nem novas interações.
7. Apenas o solicitante e o técnico atribuído podem registrar interações.
8. Não é permitido excluir setor ou categoria que tenha chamados vinculados.

Como não há autenticação, o usuário que executa a ação é informado no corpo da requisição (`solicitanteId`, `tecnicoId` ou `autorId`).

## Endpoints

| Método e rota | Descrição | Sucesso |
|---------------|-----------|---------|
| `POST /setores` | cria setor (sigla única) | 201 |
| `GET /setores` | lista setores · filtro: `nome` | 200 |
| `GET /setores/{id}` | busca setor | 200 |
| `GET /setores/{id}/categorias` | categorias de um setor | 200 |
| `PUT /setores/{id}` | atualiza setor | 200 |
| `DELETE /setores/{id}` | exclui setor (regra 8) | 204 |
| `POST /categorias` | cria categoria | 201 |
| `GET /categorias` | lista categorias · filtros: `nome`, `setorId` | 200 |
| `GET /categorias/{id}` | busca categoria | 200 |
| `PUT /categorias/{id}` | atualiza categoria | 200 |
| `DELETE /categorias/{id}` | exclui categoria (regra 8) | 204 |
| `POST /usuarios` | cria usuário (email único) | 201 |
| `GET /usuarios` | lista usuários · filtros: `nome`, `tipo`, `setorId` | 200 |
| `GET /usuarios/{id}` | busca usuário | 200 |
| `PUT /usuarios/{id}` | atualiza usuário | 200 |
| `GET /usuarios/{id}/chamados` | chamados abertos pelo solicitante ou atribuídos ao técnico · filtro: `status` | 200 |
| `POST /chamados` | abre chamado | 201 |
| `GET /chamados` | lista chamados · filtros: `status`, `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio`, `dataFim` | 200 |
| `GET /chamados/{id}` | detalha chamado com suas interações | 200 |
| `PATCH /chamados/{id}/assumir` | técnico assume | 200 |
| `PATCH /chamados/{id}/resolver` | técnico resolve informando a solução | 200 |
| `PATCH /chamados/{id}/cancelar` | solicitante cancela | 200 |
| `POST /chamados/{id}/interacoes` | registra interação | 201 |

As ações sobre o chamado usam **PATCH** porque alteram apenas parte dele (status e campos relacionados), e os `POST` de criação devolvem o cabeçalho `Location` com a URL do novo recurso.

### Coleção de requisições

O arquivo [`chamados.http`](chamados.http) contém todas as requisições acima, incluindo os casos de erro de cada regra. No IntelliJ, basta abri-lo e clicar em ▶ (ou *Run All*) com a API rodando. As requisições que criam recursos guardam o `id` retornado para as seguintes, então o arquivo deve ser executado de cima para baixo.

### Documentação OpenAPI (Swagger)

Com a API rodando, a documentação é gerada automaticamente a partir dos controllers e DTOs pelo springdoc-openapi:

- **Swagger UI**: http://localhost:8080/swagger-ui.html — permite testar cada endpoint pelo navegador (*Try it out*).
- **Documento OpenAPI (JSON)**: http://localhost:8080/v3/api-docs — pode ser importado no Swagger Editor ou no Postman.

### Dados iniciais

| Setores | Categorias | Usuários |
|---------|------------|----------|
| 1 — COTIC | 1 — Rede e Internet (COTIC) | 1, 2, 5, 6 — solicitantes |
| 2 — MANUT | 2 — Equipamentos de Informática (COTIC) | |
| | 3 — Elétrica (MANUT) | 3, 7 — técnicos da COTIC |
| | 4 — Climatização (MANUT) | 4, 8 — técnicos da MANUT |

## Exemplos de requisição

### Abrir um chamado

```http
POST /chamados
Content-Type: application/json

{
  "titulo": "Wi-Fi do laboratório 3 sem conexão",
  "descricao": "Nenhum computador do laboratório consegue acessar a internet desde a manhã",
  "local": "Bloco B - Laboratório 3",
  "categoriaId": 1,
  "solicitanteId": 1
}
```

Resposta `201 Created` (sem `prioridade` no corpo, o chamado nasce `MEDIA`):

```json
{
  "id": 1,
  "titulo": "Wi-Fi do laboratório 3 sem conexão",
  "prioridade": "MEDIA",
  "status": "ABERTO",
  "dataAbertura": "2026-10-25T22:23:03.012863",
  "dataFechamento": null,
  "solicitanteId": 1,
  "solicitanteNome": "Andy Merchan",
  "categoriaId": 1,
  "categoriaNome": "Rede e Internet",
  "setorSigla": "COTIC",
  "tecnicoId": null
}
```

*(alguns campos omitidos)*

### Assumir, interagir e resolver

```http
PATCH /chamados/1/assumir
Content-Type: application/json

{ "tecnicoId": 3 }
```

```http
POST /chamados/1/interacoes
Content-Type: application/json

{ "autorId": 3, "texto": "Verificando o switch do laboratório" }
```

```http
PATCH /chamados/1/resolver
Content-Type: application/json

{ "tecnicoId": 3, "solucao": "Switch do laboratório substituído e conexão restabelecida" }
```

`GET /chamados/1` passa a mostrar o chamado `RESOLVIDO`, com `dataFechamento`, `solucao` e a lista de `interacoes`:

```json
{
  "id": 1,
  "status": "RESOLVIDO",
  "dataFechamento": "2026-10-25T22:23:03.659631",
  "solucao": "Switch do laboratório substituído e conexão restabelecida",
  "tecnicoId": 3,
  "tecnicoNome": "David Parraga",
  "interacoes": [
    {
      "id": 1,
      "texto": "Verificando o switch do laboratório",
      "dataHora": "2026-10-25T22:23:03.355887",
      "autorId": 3,
      "autorNome": "David Parraga"
    }
  ]
}
```

*(alguns campos omitidos)*

### Paginação e filtros

Todas as listagens são paginadas com `Pageable` (parâmetros `page`, `size` e `sort`). O tamanho padrão é **10** e o máximo é **50** (um `size` maior é reduzido para 50). Os filtros são opcionais e combináveis; filtro não informado é ignorado. As datas usam o formato ISO e são inclusivas.

```http
GET /chamados?status=ABERTO&setorId=1&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc
GET /chamados?dataInicio=2026-10-01&dataFim=2026-10-31
GET /usuarios?tipo=TECNICO&setorId=1
```

Formato da resposta:

```json
{
  "itens": [ { "id": 2, "titulo": "Ar-condicionado da sala 10 pingando", "status": "CANCELADO" } ],
  "pagina": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1
}
```

### Erros

Todo erro devolve o mesmo formato JSON, gerado pelo `GlobalExceptionHandler` (`@RestControllerAdvice`):

| Código | Quando |
|--------|--------|
| **400** | dados inválidos: campo obrigatório vazio, email mal formatado, enum ou data inválidos na query string |
| **404** | recurso inexistente |
| **409** | violação de regra de negócio (transição inválida, técnico de outro setor...) ou email/sigla duplicados |

```json
{
  "timestamp": "2026-10-25T21:52:06.74922668",
  "status": 409,
  "mensagem": "O técnico só pode assumir chamados de categorias do seu setor",
  "caminho": "/chamados/1/assumir"
}
```

## Flyway e migrations

O esquema do banco é criado **somente** pelas migrations em `src/main/resources/db/migration`. O Hibernate não cria nem altera tabelas: a aplicação usa `spring.jpa.hibernate.ddl-auto=validate`, que apenas confere na inicialização se as entidades correspondem ao banco (se não corresponderem, a aplicação não sobe).

| Migration | Conteúdo |
|-----------|----------|
| `V1__criar_tabelas.sql` | tabelas, chaves estrangeiras e restrições |
| `V2__inserir_dados_iniciais.sql` | setores, categorias e usuários iniciais |
| `V3__atualizar_usuarios.sql` | corrige os usuários da V2 e adiciona mais quatro para testar os filtros |

### Como o Flyway registra as versões (`flyway_schema_history`)

Ao iniciar, o Flyway cria (se ainda não existir) a tabela `flyway_schema_history` e grava nela uma linha para cada migration aplicada: versão, descrição, nome do script, **checksum**, usuário, data, tempo de execução e se teve sucesso. A cada inicialização, ele compara os arquivos da pasta com essa tabela e executa, em ordem, apenas as versões que ainda não foram aplicadas. Por isso, na segunda vez que a API sobe, o log mostra *"Schema is up to date. No migration necessary"*.

Conteúdo da tabela neste projeto:

```
 installed_rank | version |      description       |             script             |  checksum  | success
----------------+---------+------------------------+--------------------------------+------------+---------
              1 | 1       | criar tabelas          | V1__criar_tabelas.sql          |  594033270 | t
              2 | 2       | inserir dados iniciais | V2__inserir_dados_iniciais.sql | 1718749197 | t
              3 | 3       | atualizar usuarios     | V3__atualizar_usuarios.sql     |  666497188 | t
```

### O que acontece se o checksum de uma migration aplicada mudar

O checksum é um número calculado a partir do conteúdo do arquivo SQL. Se alguém editar uma migration que já foi aplicada, o checksum do arquivo deixa de bater com o que está gravado em `flyway_schema_history`. Na próxima inicialização, a validação do Flyway falha com um erro de *checksum mismatch* e **a aplicação não sobe**.

Isso é proposital: o banco já foi alterado pela versão antiga do script, e o Flyway não tem como saber se a mudança deveria ser reaplicada, nem como desfazê-la. A regra, então, é **nunca editar uma migration aplicada**: qualquer mudança vira uma nova versão (foi o que aconteceu com a `V3`, que ajusta dados da `V2` em vez de alterá-la). O comando `flyway repair` atualiza o checksum gravado, mas só deve ser usado quando se tem certeza de que a alteração não muda o resultado (por exemplo, um comentário).

### Por que não usar `ddl-auto=update` em produção

- **Não há histórico nem revisão:** o Hibernate altera o banco "por conta própria", sem um script versionado no Git que possa ser revisado e reproduzido em outros ambientes.
- **É incompleto:** ele só adiciona tabelas e colunas. Não remove nem renomeia colunas, não migra dados e pode deixar o esquema diferente em cada ambiente.
- **É arriscado:** uma mudança simples na entidade (como renomear um atributo) pode criar uma coluna nova e deixar os dados antigos esquecidos na coluna anterior, sem nenhum aviso.
- **Não há controle de quando e como acontece:** em produção, as mudanças de esquema precisam ser previsíveis, testadas antes e aplicadas na mesma ordem em todos os ambientes, que é exatamente o que as migrations do Flyway garantem.
