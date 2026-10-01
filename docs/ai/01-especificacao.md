# 01 — Especificação

Fonte: documento "Avaliação - Back-end" (Outsera).

## Objetivo

API RESTful para leitura da lista de indicados e vencedores da categoria **Pior Filme** do Golden Raspberry Awards.

## Critérios de aceite

- [x] **CA1:** ao iniciar, a aplicação lê o CSV de filmes e insere os dados em um banco de dados.
- [x] **CA2:** o banco é em memória, com SGBD embarcado (H2), sem nenhuma instalação externa.
- [x] **CA3:** existe um endpoint que retorna o produtor com o maior intervalo entre dois prêmios consecutivos e o que obteve dois prêmios mais rápido, no formato:
  ```json
  {
    "min": [{ "producer": "...", "interval": 1, "previousWin": 2008, "followingWin": 2009 }],
    "max": [{ "producer": "...", "interval": 99, "previousWin": 1900, "followingWin": 1999 }]
  }
  ```
- [x] **CA4:** `min` e `max` são listas e retornam todos os empates.
- [x] **CA5:** o resultado é correto para qualquer conjunto de dados de entrada, não apenas para o arquivo fornecido. O caminho do CSV é configurável.
- [x] **CA6:** o web service segue o nível 2 de maturidade de Richardson: recursos, verbos HTTP e status codes adequados.
- [x] **CA7:** existem somente testes de integração, garantindo que os dados obtidos estão de acordo com os dados da proposta.
- [x] **CA8:** há um README com instruções para rodar o projeto e os testes.
- [x] **CA9:** o código está em um repositório git.

## Análise do arquivo `movielist.csv`

| Característica | Observação |
|---|---|
| Formato | UTF-8, separado por `;`, cabeçalho `year;title;studios;producers;winner` |
| Volume | 206 filmes, 42 vencedores |
| `winner` | `yes` para vencedor, vazio para indicado |
| `producers` | Vários nomes separados por `, ` e ` and ` (ex.: `Bob Cavallo, Joe Ruffalo and Steve Fargnoli`) |
| `studios` | Vários nomes separados por `, ` |
| Anos com mais de um vencedor | 1986, 1990 e 2015 |

## Resultado esperado com o arquivo da proposta

Calculado antes da implementação e usado como asserção do teste de integração principal:

| | Produtor | Intervalo | Vitórias |
|---|---|---|---|
| min | Joel Silver | 1 | 1990 → 1991 |
| max | Matthew Vaughn | 13 | 2002 → 2015 |
