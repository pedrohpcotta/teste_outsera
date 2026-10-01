# Golden Raspberry Awards API

API RESTful para leitura da lista de indicados e vencedores da categoria **Pior Filme** do Golden Raspberry Awards.

Ao iniciar, a aplicação lê o arquivo CSV de filmes e carrega os dados em um banco H2 em memória. Nenhuma instalação externa além do Java é necessária.

## Requisitos

- Java 21

O Maven Wrapper (`./mvnw`) já está incluso no projeto.

## Executando a aplicação

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

Por padrão é usado o arquivo `src/main/resources/movielist.csv`. Para usar outro arquivo:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.movies.csv-path=file:/caminho/para/arquivo.csv"
```

Ou, a partir do jar:

```bash
./mvnw package
java -jar target/golden-raspberry-awards-1.0.0.jar --app.movies.csv-path=file:/caminho/para/arquivo.csv
```

### Formato do CSV

Separado por `;`, codificação UTF-8, com o cabeçalho `year;title;studios;producers;winner`.

- `studios`: estúdios separados por vírgula.
- `producers`: produtores separados por vírgula e/ou ` and ` (ex.: `Bob Cavallo, Joe Ruffalo and Steve Fargnoli`).
- `winner`: `yes` para vencedor; qualquer outro valor (inclusive vazio) indica apenas indicado.

Se o arquivo for inválido (cabeçalho ausente, ano não numérico, título vazio ou número de colunas inconsistente), a aplicação não inicia e informa a linha com problema.

## Endpoints

### `GET /producers/award-intervals`

Retorna os produtores com o menor e o maior intervalo entre dois prêmios consecutivos.

```json
{
  "min": [
    { "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }
  ],
  "max": [
    { "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }
  ]
}
```

Regras:

- Apenas filmes vencedores são considerados.
- Os intervalos são calculados entre vitórias consecutivas de um mesmo produtor.
- Em caso de empate, todos os produtores empatados são retornados, ordenados por nome e ano.
- Um mesmo produtor pode aparecer em `min` e `max`.
- Duas vitórias do mesmo produtor no mesmo ano geram intervalo `0`.
- Se nenhum produtor tiver ao menos duas vitórias, `min` e `max` são listas vazias.

### `GET /movies`

Lista paginada de filmes.

| Parâmetro | Descrição |
|---|---|
| `year` | Filtra pelo ano |
| `winner` | `true` para vencedores, `false` para não vencedores |
| `page`, `size` | Paginação (padrão: `page=0`, `size=20`) |
| `sort` | Ordenação (padrão: `year,asc`), ex.: `sort=title,desc` |

Exemplo: `GET /movies?year=1990&winner=true`

### `GET /movies/{id}`

Retorna um filme. Responde `404` caso não exista.

Erros seguem o formato Problem Details (RFC 9457).

## Testes

O projeto contém apenas testes de integração. Eles sobem a aplicação completa com o banco em memória e validam as respostas da API contra o arquivo da proposta e contra arquivos de cenário em `src/test/resources/datasets` (empates, intervalo zero, mesmo produtor em `min` e `max`, separadores de produtores, ausência de intervalos).

Executar todos os testes:

```bash
./mvnw test
```

Executar uma classe ou um teste específico:

```bash
./mvnw test -Dtest=ProducerAwardIntervalsIntegrationTest
./mvnw test -Dtest='MovieIntegrationTest#filtersWinnersByYear'
```

## Uso de IA

O desenvolvimento foi feito com apoio do Claude Code, seguindo Spec-Driven Development. A especificação, as decisões, o plano e o log das interações estão em [`docs/ai`](docs/ai/README.md).
