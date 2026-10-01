# 03 — Plano e tarefas

## Stack

Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Bean Validation, H2 em memória, Apache Commons CSV, Maven.

## Arquitetura

Organização por feature:

| Pacote | Responsabilidade |
|---|---|
| `loader` | Leitura e validação do CSV (`MovieCsvReader`) e carga no banco na inicialização (`MovieDataLoader`, um `ApplicationRunner`) |
| `movie` | Entidade `Movie`, `GET /movies` paginado com filtros e `GET /movies/{id}` |
| `producer` | Entidade `Producer` e cálculo dos intervalos em `GET /producers/award-intervals` |
| `studio` | Entidade `Studio` |
| `common` | Tratamento de erros não coberto pelo Problem Details nativo do Spring |

## Modelo de dados

`Movie` se relaciona com `Producer` e `Studio` via `@ManyToMany`, de forma normalizada. Produtores e estúdios são deduplicados por nome durante a carga.

## Algoritmo dos intervalos

1. Uma query JPQL retorna os pares `(produtor, ano)` apenas dos filmes vencedores, já ordenados por produtor e ano.
2. Uma única passada sobre a lista gera os intervalos entre vitórias consecutivas do mesmo produtor. Custo O(n) após a ordenação, que é feita pelo banco.
3. O menor e o maior intervalo são identificados e todos os empates de cada um são retornados.

## Decisões técnicas

- `open-in-view` desligado. O mapeamento para DTO acontece dentro de serviços `@Transactional(readOnly = true)`.
- `hibernate.default_batch_fetch_size` evita N+1 ao carregar produtores e estúdios na listagem.
- Paginação serializada como DTO estável (`content` + `page`).
- Erros no formato Problem Details (RFC 9457): `404` para filme inexistente, `400` para parâmetro ou ordenação inválidos.
- Caminho do CSV configurável por `app.movies.csv-path` (`classpath:` ou `file:`), para permitir a avaliação com outros conjuntos de dados.

## Tarefas

- [x] T1: Scaffold do projeto (Spring Boot, Maven Wrapper, configuração do H2)
- [x] T2: Entidades `Movie`, `Producer` e `Studio` e repositórios
- [x] T3: Leitura do CSV com validação e carga na inicialização
- [x] T4: Endpoint de intervalos de premiação com tratamento de empates
- [x] T5: Endpoints de filmes com paginação, filtros e Problem Details
- [x] T6: Testes de integração com o arquivo da proposta e com cenários de borda
- [x] T7: README com instruções de execução e testes

## Estratégia de testes

Somente testes de integração (`@SpringBootTest` + MockMvc), com a aplicação completa e o banco em memória.

| Cenário | Dataset |
|---|---|
| Arquivo da proposta | `movielist.csv` |
| Empates no mínimo e no máximo | `datasets/ties.csv` |
| Mesmo produtor em `min` e `max`, com três ou mais vitórias | `datasets/same-producer-min-and-max.csv` |
| Duas vitórias no mesmo ano (intervalo 0) | `datasets/same-year-wins.csv` |
| Separadores de produtores e variações de `winner` | `datasets/producer-separators.csv` |
| Um único intervalo | `datasets/single-interval.csv` |
| Nenhum intervalo | `datasets/no-intervals.csv` |
| Listagem, filtros, 404 e 400 | `movielist.csv` |
