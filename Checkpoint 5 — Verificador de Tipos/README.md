# Checkpoint 5 — Verificador de Tipos

## 1. Objetivo

O Checkpoint 5 implementa a análise semântica de tipos de uma linguagem de programação educacional.

O verificador percorre a Árvore Sintática Abstrata (AST), determina o tipo das expressões e identifica operações incompatíveis, chamadas de funções incorretas, problemas de atribuição e violações das regras de retorno.

A implementação utiliza o padrão de projeto **Visitor**, reaproveitando as estruturas construídas nos checkpoints anteriores.

## 2. Organização do compilador

O processamento do programa ocorre nas seguintes etapas:

1. **Análise léxica:** `Scanner.java` transforma o código-fonte em tokens.
2. **Análise sintática:** `Parser.java` verifica a estrutura gramatical e constrói a AST.
3. **Identificação:** `VisitorIdentificacao.java` resolve referências a variáveis e funções utilizando a tabela de símbolos.
4. **Preparação dos tipos:** `VisitorPreparacaoTipos.java` associa tipos e assinaturas aos símbolos identificados.
5. **Verificação de tipos:** `VisitorVerificadorTipos.java` percorre a AST, registra os tipos das expressões e produz diagnósticos semânticos.

A classe `Type.java` representa os tipos da linguagem, enquanto `FunctionType.java` armazena as assinaturas de funções.

## 3. Tipos suportados

A linguagem possui os seguintes tipos:

| Tipo | Significado |
|---|---|
| `number` | Número inteiro |
| `real` | Número real |
| `bool` | Valor booleano |
| `char` | Caractere |
| `string` | Sequência de caracteres |
| `void` | Ausência de valor de retorno |

Internamente, o verificador também utiliza `ERROR`, que permite representar uma expressão inválida e reduzir a propagação de mensagens de erro em cascata.

## 4. Principais regras semânticas

### 4.1. Compatibilidade numérica

A linguagem permite a promoção implícita de `number` para `real`, mas não permite automaticamente a conversão inversa.

Essa regra é aplicada em atribuições, inicializações, argumentos e retornos de funções.

### 4.2. Operadores

- Operações aritméticas são permitidas entre operandos numéricos compatíveis.
- O operador `%` exige operandos `number`.
- Operadores lógicos exigem operandos `bool`.
- Operações relacionais seguem as combinações definidas na tabela de tipos.
- A concatenação de strings é permitida pelo operador `+`.
- Comparações de igualdade permitem tipos iguais e combinações numéricas compatíveis.
- Combinações incompatíveis produzem erros semânticos.

### 4.3. Funções e procedimentos

As funções possuem um tipo de retorno e uma lista de parâmetros.

O verificador confere:

- Quantidade e tipos dos argumentos.
- Compatibilidade entre o valor retornado e o tipo declarado.
- Presença de retorno nos caminhos necessários de funções não `void`.
- Uso correto de procedimentos `void`.
- Uso de chamadas como comando ou expressão, conforme as regras da linguagem.

### 4.4. Inicialização de variáveis

A implementação realiza uma análise conservadora de inicialização.

Uma variável potencialmente utilizada antes de receber um valor gera um **aviso**, sem ser tratada automaticamente como erro de tipagem.

A análise considera declarações, atribuições, leitura de dados, condicionais, laços e sombreamento.

## 5. Decisões de projeto

As principais decisões adotadas são:

1. Promoção implícita de `number` para `real`, sem promoção inversa.
2. Proibição de operações aritméticas entre `char` e tipos numéricos.
3. Ausência de conversão implícita de números em booleanos.
4. Concatenação de strings com `+`, sem ordenação de strings.
5. Igualdade entre tipos diferentes permitida apenas nas combinações numéricas compatíveis.
6. Funções que retornam valores não podem ser chamadas como comandos isolados.
7. Recursão direta permitida; recursão mútua depende de declaração prévia.
8. Promoção `number` para `real` permitida nos argumentos de funções.
9. Funções não `void` devem garantir retorno nos caminhos necessários.
10. Sobrecarga de funções não permitida.
11. O uso de um nome de função sem chamada válida não é considerado uma expressão de valor.

As regras formais e a tabela de operadores estão documentadas separadamente no material manuscrito da Parte 1.

## 6. Testes automatizados

Foram executadas cinco baterias de testes:

| Bateria | Casos aprovados |
|---|---:|
| Regressão dos Checkpoints 3 e 4 | 19/19 |
| Programas completos do Checkpoint 5 | 30/30 |
| Operadores diretamente na AST | 92/92 |
| Inicialização e avisos | 12/12 |
| Procedimentos, funções e retornos | 21/21 |
| **Total** | **174/174** |

Os testes de operadores, inicialização e procedimentos utilizam construções diretas da AST, permitindo verificar partes da análise semântica independentemente do parser.

Os programas completos são comparados com os respectivos resultados esperados.

**Observação:** a aprovação dos testes representa os cenários implementados nas baterias, não uma garantia de cobertura absoluta do compilador.

## 7. Como executar

**Ambiente utilizado:** IntelliJ IDEA Community Edition, com OpenJDK 23.

Para executar as verificações:

1. Abra o projeto no IntelliJ.
2. Confirme que o JDK está configurado.
3. Compile o projeto.
4. Execute o método `main()` de cada classe de teste.

Classes de teste:

- `Checkpoint4Suite.java`
- `TesteCheckpoint5.java`
- `TesteOperadoresAST.java`
- `TesteInicializacao.java`
- `TesteProcedimentosERetornos.java`

As suítes utilizam a saída do console para apresentar os testes aprovados e os diagnósticos de falha.

## 8. Organização dos testes

A pasta `test/` contém os programas utilizados na verificação, os resultados esperados e as classes Java de teste.

Os testes de regressão do CP4 ficam separados dos programas e resultados esperados do CP5 para evitar conflitos de nomes e facilitar a manutenção.

A execução de testes que leem arquivos utiliza caminhos relativos à raiz do projeto, que deve estar configurada como diretório de trabalho.

## 9. Limitações

O projeto implementa a análise semântica exigida para o checkpoint, não um compilador completo com geração de código executável.

A análise de inicialização é conservadora e não representa uma prova de inicialização para todos os caminhos de execução possíveis.

As regras de compatibilidade seguem as decisões de projeto adotadas para esta linguagem, que não necessariamente correspondem às regras de Java ou de outras linguagens de programação.

