# calculadoracientifica

LINK DO VIDEO DO YOUTUBE: https://youtu.be/6iGeZ_PpY3E 

# Calculadora Científica para Android

Uma aplicação de calculadora científica desenvolvida em Kotlin para a plataforma Android. Oferece funcionalidades de cálculo padrão e científico, com uma interface intuitiva e responsiva.

## Visão Geral

Este projeto implementa uma calculadora científica capaz de realizar operações aritméticas básicas, funções trigonométricas, logarítmicas, exponenciais, fatoriais, e outras operações científicas. A interface foi construída utilizando XML para o layout e Kotlin para a lógica da aplicação. A biblioteca `exp4j` é utilizada para a avaliação das expressões matemáticas.

## Funcionalidades Principais

* **Operações Aritméticas Básicas:** Adição (`+`), Subtração (`-`), Multiplicação (`×`), Divisão (`÷`).
* **Operações Científicas:**
    * Trigonométricas: `sin`, `cos`, `tan`.
    * Logarítmicas: `ln` (logaritmo natural), `log` (logaritmo base 10).
    * Exponenciais: `x^y` (potenciação), `e` (constante de Euler).
    * Radiciação: `√` (raiz quadrada).
    * Fatorial: `n!`.
    * Constantes: `π` (Pi), `e` (Euler).
* **Funções Inversas (Modo INV):**
    * `asin`, `acos`, `atan`
    * `exp` (inverso de `ln`)
    * `10^x` (inverso de `log10`)
    * `x^2` (inverso de `sqrt`)
    * Raiz n-ésima (usando `x^(1/y)` no modo INV para `x^y`).
* **Gerenciamento de Expressão:**
    * Uso de parênteses `(` `)` para definir a ordem das operações.
    * Fechamento automático de parênteses ao calcular, se necessário.
    * Multiplicação implícita (ex: `2π`, `3sin(45)`, `(2+3)(1+1)`).
* **Controles de Exibição:**
    * Display para a expressão completa.
    * Display para a entrada atual ou resultado.
    * Limite de entrada de caracteres (`MAX_INPUT_LENGTH = 40`).
* **Modos de Cálculo:**
    * Modo Graus (`DEG`) e Radianos (`RAD`) para funções trigonométricas.
* **Funções de Memória:**
    * `MR` (Memory Recall): Recupera o valor da memória.
    * `M+` (Memory Add): Adiciona o resultado atual ao valor na memória.
    * `M-` (Memory Subtract): Subtrai o resultado atual do valor na memória.
* **Controles Adicionais:**
    * `CE` (Clear Entry): Limpa a entrada atual e a expressão, resetando para "0".
    * Botão de Ponto Decimal (`.`).
* **Tratamento de Erros:**
    * Exibe mensagens como "Erro (Div/0)", "Erro (Entrada)", "Erro (Indef.)", "Erro (Infinito)".
    * Logs detalhados para debugging (`LogCat`).

## Tecnologias Utilizadas

* **Linguagem:** Kotlin
* **Plataforma:** Android SDK
* **Interface do Usuário (UI):** XML para layouts e estilos.
* **Avaliação de Expressões:** Biblioteca [exp4j](https://www.objecthunter.net/exp4j/)
* **Componentes Visuais:** Material Components (para botões e tema base).

## Estrutura do Projeto (Simplificada)

* `app/src/main/java/com/example/calculadoracientifica/MainActivity.kt`: Contém toda a lógica da calculadora, incluindo o tratamento de eventos dos botões, a lógica de cálculo e a atualização da interface.
* `app/src/main/res/layout/activity_main.xml`: Define a interface gráfica da calculadora, com todos os botões e campos de texto dispostos em um `LinearLayout` e `GridLayout`.
* `app/src/main/res/values/styles.xml`: Define os estilos visuais dos botões (numéricos, de operação, científicos, etc.) e o tema da aplicação.
* `app/src/main/res/values/colors.xml`: (Não fornecido, mas referenciado) Arquivo para definir as cores usadas nos estilos e layout.
* `app/build.gradle`: (Não fornecido) Arquivo de configuração do Gradle onde a dependência `exp4j` deve ser adicionada.

## Como Compilar e Executar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/SEU_USUARIO/NOME_DO_REPOSITORIO.git](https://github.com/SEU_USUARIO/NOME_DO_REPOSITORIO.git)
    ```
2.  **Abra no Android Studio:**
    * Importe o projeto no Android Studio.
3.  **Adicione a dependência `exp4j`:**
    * No arquivo `app/build.gradle`, adicione a seguinte linha na seção `dependencies`:
      ```gradle
      implementation 'net.objecthunter:exp4j:0.4.8' // Ou a versão mais recente
      ```
    * Sincronize o projeto com os arquivos Gradle.
4.  **Crie o arquivo `colors.xml`:**
    * Em `app/src/main/res/values/`, crie `colors.xml` com as definições de cores referenciadas nos arquivos de layout e estilos (ex: `@color/dark_background`, `@color/display_text_primary`, `@color/number_button`, etc.). Exemplo básico:
      ```xml
      <?xml version="1.0" encoding="utf-8"?>
      <resources>
          <color name="dark_background">#FF202020</color>
          <color name="display_text_primary">#FFFFFFFF</color>
          <color name="display_text_secondary">#FFBBBBBB</color>
          <color name="button_text">#FFFFFFFF</color>
          <color name="number_button">#FF4CAF50</color>
          <color name="operation_button">#FFFF9800</color>
          <color name="scientific_button">#FF3F51B5</color>
          <color name="function_button">#FF607D8B</color>
          <color name="equals_button">#FF00BCD4</color>
          <color name="inverse_mode_active">#FFFFEB3B</color> </resources>
      ```
5.  **Compile e Execute:**
    * Construa o projeto (Build > Make Project).
    * Execute a aplicação em um emulador Android ou em um dispositivo físico.

## Detalhes da Implementação

### Avaliação de Expressões
A biblioteca `exp4j` é o coração do cálculo. A `MainActivity` prepara a string da expressão:
* Substitui `π` e `e` por seus valores numéricos.
* Converte `√` para `sqrt`.
* Converte `ln(` para `log(` (exp4j usa `log` para ln).
* Trata o fatorial `n!` convertendo-o para uma função customizada `fact(n)`.
* Ajusta as funções trigonométricas para o modo Graus/Radianos, convertendo os ângulos apropriadamente antes de passar para `exp4j`.

### Funções Customizadas
* **Fatorial (`fact`):** Implementada como uma função customizada para `exp4j`, lidando com números inteiros não negativos.

### Interface do Usuário
* O layout é definido em `activity_main.xml` usando `GridLayout` para organizar os botões de forma eficiente.
* Os estilos dos botões são centralizados em `styles.xml`, permitindo fácil customização da aparência.
* O estado do botão `INV` muda de cor para indicar se o modo de funções inversas está ativo.

### Formatação de Resultado
Os resultados são formatados para exibição:
* Números inteiros são mostrados sem casas decimais.
* Números decimais são mostrados com até 8 casas decimais (usando `Locale.US` para garantir o ponto decimal), com zeros à direita e ponto decimal final removidos.
* Limitação ao `MAX_INPUT_LENGTH` para evitar overflow no display.

## Possíveis Melhorias Futuras

* **Histórico de Cálculos:** Salvar e exibir cálculos anteriores.
* **Temas:** Permitir que o usuário escolha entre diferentes temas visuais.
* **Funções Adicionais:** Incorporar mais funções científicas e estatísticas.
* **Conversor de Unidades:** Adicionar funcionalidade de conversão de unidades.
* **Validação de Entrada Aprimorada:** Feedback visual mais direto para entradas inválidas.
* **Acessibilidade:** Melhorar os recursos de acessibilidade (descrições de conteúdo, navegação).
* **Testes Unitários e de Interface:** Adicionar testes para garantir a robustez.

## Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou submeter pull requests.

---

*Este README foi gerado com base nos arquivos de código fornecidos.*
*Última atualização do modelo: 11 de maio de 2025.*
