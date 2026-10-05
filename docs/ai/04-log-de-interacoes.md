# 04 — Log de interações

Registro cronológico das interações com o agente (Claude Code) que tiveram impacto no projeto. Os prompts estão transcritos com a grafia corrigida.

---

## 1. Contexto inicial

**Prompt:** `/init`

**Resultado:** com a pasta contendo apenas o PDF da avaliação e o CSV, o agente leu os dois e gerou um `CLAUDE.md` inicial, um arquivo local de contexto para o agente. Esse arquivo trazia os requisitos, o formato de resposta esperado e as particularidades do CSV: separador `;`, `winner` vazio para indicados e produtores separados por `,` e ` and `.

---

## 2. Análise da especificação e debate

**Prompt:**
> Leia o arquivo Avaliação - Back-end.pdf e vamos debater a respeito. A ideia é construir algo no backend nas APIs, com Java 21 e Spring Boot, Spring REST, Spring Data etc. Entenda o contexto dos requisitos e me dê sugestões de como fazer.

**Resultado:**
- Identificação do ponto mais crítico da avaliação: o resultado precisa estar correto para outros conjuntos de dados. Por isso o caminho do CSV deve ser configurável.
- Cálculo do resultado esperado a partir do CSV (Joel Silver: 1; Matthew Vaughn: 13), usado depois como asserção do teste principal.
- Proposta de stack, modelo de dados, algoritmo e estratégia de testes só de integração, usando datasets de cenário.
- Levantamento de casos de borda: anos com mais de um vencedor (1986, 1990, 2015), mesmo produtor vencendo duas vezes no mesmo ano, nenhum intervalo, separadores mistos.
- Quatro perguntas de decisão (ver [02-clarificacoes.md](02-clarificacoes.md)).

**Revisão:** avaliei a proposta e respondi às decisões em aberto.

---

## 3. Decisões e início da implementação

**Prompt:**
> 1 - sim; 2 - acho que seria bom incluirmos; 3 - maven; 4 - português. Além disso, não coloque comentários no código. De resto, pode seguir com as implementações de acordo com o que definimos aqui.

**Resultado:**
- Projeto criado conforme o [plano](03-plano-e-tarefas.md): entidades, carga do CSV, endpoint de intervalos, endpoints de filmes e tratamento de erros.
- Ajuste feito durante a implementação: ao analisar o CSV, o agente separou as regras de divisão de nomes. Produtores são divididos por `,` e ` and `; estúdios, apenas por `,`.
- Seis CSVs de cenário criados em `src/test/resources/datasets/`.

**Problemas encontrados na primeira execução dos testes e corrigidos:**

| Sintoma | Causa | Correção |
|---|---|---|
| Testes de `/movies` recebiam os dados de outro dataset (10 filmes em vez de 206) | Com uma URL fixa do H2, os contextos de teste com propriedades diferentes compartilhavam o mesmo banco, e a última carga sobrescrevia as anteriores | `spring.datasource.generate-unique-name: true`, um banco por contexto |
| Testes de cenário recebiam o resultado do dataset oficial | As classes `@Nested` usavam o `MockMvc` injetado na classe externa, que pertence ao contexto padrão | Cada classe aninhada passou a injetar o próprio `MockMvc` |

**Verificação:**
- 13 testes de integração passando.
- Aplicação executada via jar, com chamadas reais aos endpoints:
  - `/producers/award-intervals` com o CSV oficial;
  - `/producers/award-intervals` com um CSV externo, via `--app.movies.csv-path=file:...`;
  - `404` em `/movies/{id}` inexistente;
  - listagem filtrada de vencedores.
- Confirmado que `./mvnw test -Dtest=ProducerAwardIntervalsIntegrationTest` executa também as classes aninhadas, como documentado no README.

---

## 4. Registro do uso de IA

**Prompt:**
> Me mostre como ficariam os logs, por gentileza.

**Resultado:** organização deste registro no formato de Spec-Driven Development (especificação, clarificações, plano/tarefas e log), a partir das interações reais do desenvolvimento.

---

## 5. Revisão após retorno da avaliação

**Prompt:**
> Foi me dado de retorno o seguinte item observado: o sistema realiza 5+ loops em código para chegar ao resultado. Confira, a fim de encontrar o problema e revisar o código em conjunto.

**Resultado:** o agente confirmou o apontamento. O plano previa uma única passada, mas `ProducerAwardIntervalService` percorria os dados cinco vezes e ordenava duas:

| Passada | Operação |
|---|---|
| 1 | `for` montando a lista completa de intervalos |
| 2 | `stream().min()` sobre essa lista |
| 3 | `stream().max()` sobre a mesma lista |
| 4 | `filter` + `sorted` para os empates do menor intervalo |
| 5 | `filter` + `sorted` para os empates do maior intervalo |

**Correção:**
- Um único `for` sobre as vitórias retornadas pelo banco, sem lista intermediária. Ao encontrar um intervalo menor que o mínimo atual, a lista `min` é limpa; se for igual, o intervalo é adicionado. O mesmo vale para `max`.
- A ordenação em Java foi removida: a query já ordena por produtor e ano, então os intervalos saem na ordem exigida pela resposta (produtor e `previousWin`).
- `AwardIntervalsResponse.empty()` foi removido por ter ficado sem uso; sem intervalos, o próprio loop retorna `{"min":[],"max":[]}`.
- O [plano](03-plano-e-tarefas.md) foi atualizado para descrever o algoritmo como implementado.

**Alternativa avaliada e descartada:** calcular tudo em SQL com `LAG()` e filtros de mínimo e máximo. Eliminaria os loops em Java, mas deixaria a query bem menos legível, e a passada única já resolve o apontamento.

**Revisão:** o agente chegou a incluir um Javadoc explicando a dependência da ordenação feita pelo banco, mas ele foi removido para manter a decisão da interação 3 (código sem comentários). A justificativa fica registrada aqui e no plano.

**Verificação:** os 13 testes de integração continuam passando, inclusive os cenários de empate, intervalo zero, mesmo produtor em `min` e `max` e ausência de intervalos.

---

## 6. Revisão contra os critérios da avaliação

**Prompt:**
> Revise o documento de critérios do problema e veja se tem algum outro ponto de falha no projeto, para que possamos seguir com as iterações.

**Resultado:** o agente comparou o projeto com cada item do PDF da avaliação, que contém as minhas anotações sobre o entendimento do requisito e é a base validada. Rodou também a suíte com o JDK 25 e subiu a aplicação com um CSV alternativo. Os pontos levantados foram priorizados, e eu escolhi quais seguir:

| # | Ponto | Decisão |
|---|---|---|
| 1 | CSV salvo com BOM (Excel/Windows) impede a aplicação de iniciar | Corrigido na interação 7 |
| 2 | A validação do CSV inválido não tinha testes | Corrigir |
| 3 | A carga fazia duas passadas (registros → lista → filmes), o mesmo tipo de problema do item 5 | Corrigir |
| 4 | Separador ` and ` e nomes diferenciavam maiúsculas | Corrigir |
| 5 | README indicava a ordenação padrão como `year,asc`; o código ordena por `year` e `id` | Corrigir |
| 6 | `CLAUDE.md` fora do repositório | Manter como está |

**Prompt:**
> Faça a 2, criando os cenários de testes para invalidar o CSV. 3 - ajuste para mantermos na passada solicitada. 4 - pode fazer. 5 - ajuste o README para se adequar à ordenação correta. 6 - pode seguir assim.

**Correções:**
- **Passada única na carga:** `MovieCsvReader.read` passou a receber um `Consumer` e entrega cada linha assim que é validada. O `MovieDataLoader` persiste o filme no mesmo loop. Produtores e estúdios novos são persistidos na primeira ocorrência e reaproveitados por um mapa em memória. Não há mais lista intermediária.
- **Maiúsculas:** o separador ` and ` passou a ignorar caixa, e a deduplicação de produtores e estúdios usa o nome em minúsculas como chave, mantendo a primeira grafia do arquivo.
- **Validação:** a mensagem de cabeçalho ausente passou a listar só as colunas faltantes, em ordem fixa (antes dependia da ordem de um `Set`). Erros de leitura durante a iteração (ex.: aspas não fechadas) também passaram a ser reportados como erro de CSV.
- **README:** ordenação padrão corrigida, além das novas regras de nomes e da lista de validações.

**Testes adicionados:**
- `InvalidMoviesCsvIntegrationTest`: sobe a aplicação com cada arquivo de `datasets/invalid/` e verifica que ela não inicia, com a mensagem e a linha corretas (arquivo inexistente, cabeçalho ausente, ano inválido, título vazio, colunas inconsistentes, aspas não fechadas).
- Cenário `producer-name-case.csv` em `ProducerAwardIntervalsIntegrationTest`: `Joel Silver` e `joel silver AND Anna Bell` resultam em um único produtor.

**Problema encontrado e corrigido:** a primeira versão dos testes de CSV inválido esperava a exceção embrulhada pelo Spring, mas o Spring Boot 3.5 relança a exceção do `ApplicationRunner` diretamente. As asserções foram ajustadas.

**Verificação:** 20 testes de integração passando (6 de filmes, 8 de intervalos, 6 de CSV inválido).

---

## 7. CSV com BOM

**Prompt:**
> O ponto 1, eu testei e realmente você tinha razão. Pode ajustá-lo, para que possamos seguir com a validação correta para este cenário.

**Contexto:** na interação 6, o agente reproduziu a falha subindo o jar com o `movielist.csv` prefixado com BOM UTF-8 (`EF BB BF`). O BOM era lido como parte do primeiro cabeçalho (`﻿year`), e a aplicação não iniciava: `Movies CSV must contain the headers [...] but found [﻿year, title, ...]`. Arquivos salvos pelo Excel ou no Windows costumam ter BOM, então esse era o risco mais direto do aviso da avaliação sobre outros conjuntos de dados. Confirmei a falha no meu ambiente.

**Correção:** `MovieCsvReader` passou a descartar o BOM, se houver, antes de entregar o conteúdo ao parser, usando um `PushbackReader` da JDK. Nenhuma dependência nova. Arquivos sem BOM seguem inalterados.

**Teste adicionado:** `byte-order-mark.csv`, gravado com os bytes `EF BB BF` no início, e o cenário correspondente em `ProducerAwardIntervalsIntegrationTest`.

**Verificação:**
- 21 testes de integração passando.
- O jar foi executado novamente com o `movielist.csv` prefixado com BOM: carregou 206 filmes e `/producers/award-intervals` retornou Joel Silver (1) e Matthew Vaughn (13), o mesmo resultado do arquivo original.
