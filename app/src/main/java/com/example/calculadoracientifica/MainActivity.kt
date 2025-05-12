package com.example.calculadoracientifica

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.util.Locale // Importação necessária para Locale

/** Activity principal da calculadora científica. */
class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView // Mostra a expressão completa.
    private lateinit var tvResult: TextView     // Mostra a entrada atual ou o resultado.

    private var currentInput: String = "0"      // Entrada atual do usuário.
    private var currentExpression: String = ""  // Última expressão calculada.
    private var memoryValue: Double = 0.0       // Valor da memória (M+).
    private var isRadiansMode: Boolean = false  // Controla modo GRAUS/RADIANOS.
    private var isInverseMode: Boolean = false  // Controla modo de funções inversas (INV).

    companion object {
        private const val MAX_INPUT_LENGTH = 40 // Limite de caracteres da entrada.
        private const val TAG = "Calculator"    // Tag para logs.
    }

    /** Inicializa a activity, layout e listeners de botões. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupNumberButtons()
        setupOperationButtons()
        setupScientificButtons()
        setupMemoryButtons()
        setupSpecialButtons()
        updateDisplay()
    }

    /** Vincula as TextViews do layout às variáveis da classe. */
    private fun initializeViews() {
        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)
    }

    /** Configura os botões numéricos (0-9 e ponto). */
    private fun setupNumberButtons() {
        val numberButtons = listOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9", R.id.btnPonto to "."
        )
        numberButtons.forEach { (buttonId, value) ->
            findViewById<Button>(buttonId).setOnClickListener {
                handleNumericInput(value)
            }
        }
    }

    /** Adiciona um valor numérico ou ponto decimal à entrada atual. */
    private fun handleNumericInput(value: String) {
        if (currentInput.length >= MAX_INPUT_LENGTH && value != "." && !currentInput.endsWith(".")) {
            showToast("Limite de caracteres atingido")
            return
        }

        if (value == ".") {
            val segments = currentInput.split(Regex("[+\\-*/()^!%]"))
            val lastSegment = segments.lastOrNull() ?: ""
            if (lastSegment.contains(".")) return // Evita múltiplos pontos no mesmo número.

            if (currentInput.isEmpty() || currentInput == "0" || !currentInput.last().isDigit()) {
                currentInput = if (currentInput == "0") "0." else currentInput + "0."
            } else {
                currentInput += value
            }
        } else if (currentInput == "0" && value != "0") {
            currentInput = value // Substitui "0" inicial.
        } else if (!(currentInput == "0" && value == "0")) { // Evita "00".
            currentInput += value
        }
        updateDisplay()
    }

    /** Configura os botões de operações básicas (+, -, *, /, parênteses). */
    private fun setupOperationButtons() {
        val operationButtons = listOf(
            R.id.btnMais to "+", R.id.btnMenos to "-", R.id.btnMultiplicar to "*",
            R.id.btnDividir to "/", R.id.btnOpenParen to "(", R.id.btnCloseParen to ")"
        )
        operationButtons.forEach { (buttonId, operator) ->
            findViewById<Button>(buttonId).setOnClickListener {
                handleOperatorInput(operator)
            }
        }
        findViewById<Button>(R.id.btnIgual).setOnClickListener { calculateResult() }
    }

    /** Adiciona um operador ou parêntese à entrada atual, validando a inserção. */
    private fun handleOperatorInput(operator: String) {
        if ((currentInput + operator).length > MAX_INPUT_LENGTH && operator != ")") {
            showToast("Limite de caracteres atingido")
            return
        }

        if (canAddOperatorOrParenthesis(operator)) {
            if (currentInput == "0" && (operator == "(" || operator == "-")) {
                currentInput = operator // Permite iniciar com "(" ou "-".
            } else {
                currentInput += operator
            }
            updateDisplay()
        }
    }

    /** Configura os botões de funções científicas (sin, cos, !, π, etc.). */
    private fun setupScientificButtons() {
        val scientificButtonsMap = mapOf(
            R.id.btnSin to "sin(", R.id.btnCos to "cos(", R.id.btnTan to "tan(",
            R.id.btnLn to "ln(", R.id.btnLog to "log10(", R.id.btnSqrt to "sqrt(",
            R.id.btnExp to "^", R.id.btnFact to "!", R.id.btnPi to "π", R.id.btnE to "e"
        )
        scientificButtonsMap.forEach { (buttonId, funcKey) ->
            findViewById<Button>(buttonId).setOnClickListener {
                val actualFuncOrOp = if (isInverseMode) getInverseFunction(funcKey) else funcKey
                dispatchScientificInput(actualFuncOrOp)
            }
        }
    }

    /** Direciona a entrada científica para a função de tratamento apropriada. */
    private fun dispatchScientificInput(key: String) {
        when {
            key == "π" -> appendConstant(Math.PI.toString(), "π")
            key == "e" -> appendConstant(Math.E.toString(), "e")
            key == "!" || key == "^" || key == "10^" || key == "^2" -> appendOperatorOrPostfix(key)
            key.endsWith("(") -> appendFunctionCall(key)
            else -> Log.w(TAG, "Chave científica não tratada: $key")
        }
    }

    /** Configura os botões de memória (MR, M+, M-). */
    private fun setupMemoryButtons() {
        findViewById<Button>(R.id.btnMRC).setOnClickListener {
            val memoryString = formatResult(memoryValue)
            if ((currentInput + memoryString).length > MAX_INPUT_LENGTH && currentInput != "0") {
                showToast("Limite de caracteres ao colar da memória")
                return@setOnClickListener
            }
            currentInput = if (currentInput == "0") memoryString else currentInput + memoryString
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMMais).setOnClickListener {
            performMemoryOperation { it + evaluateExpression(tvResult.text.toString()) }
        }
        findViewById<Button>(R.id.btnMMenos).setOnClickListener {
            performMemoryOperation { it - evaluateExpression(tvResult.text.toString()) }
        }
    }

    /** Executa uma operação na memória (soma ou subtração). */
    private fun performMemoryOperation(operation: (Double) -> Double) {
        try {
            val valueInResult = tvResult.text.toString()
            if (valueInResult != "Erro" && valueInResult.isNotBlank()) {
                memoryValue = operation(memoryValue)
                showToast("Memória atualizada: ${formatResult(memoryValue)}")
            } else {
                showToast("Resultado inválido para operação de memória.")
            }
        } catch (e: Exception) {
            showToast("Erro na operação de memória.")
            Log.e(TAG, "Erro em operação M+/-: ${e.message}", e)
        }
    }

    /** Configura botões especiais (CE, DEG/RAD, INV). */
    private fun setupSpecialButtons() {
        findViewById<Button>(R.id.btnCE).setOnClickListener {
            currentInput = "0"
            currentExpression = ""
            if (isInverseMode) {
                isInverseMode = false
                updateInverseButtonState()
            }
            updateDisplay()
        }
        findViewById<Button>(R.id.btnDegRad).setOnClickListener {
            isRadiansMode = !isRadiansMode
            (it as Button).text = if (isRadiansMode) "RAD" else "DEG"
            showToast(if (isRadiansMode) "Modo Radianos" else "Modo Graus")
        }
        findViewById<Button>(R.id.btnInverse).setOnClickListener {
            isInverseMode = !isInverseMode
            updateInverseButtonState()
            showToast(if (isInverseMode) "Modo INV ativado" else "Modo INV desativado")
        }
    }

    /** Atualiza a aparência do botão INV conforme o modo. */
    private fun updateInverseButtonState() {
        val inverseBtn = findViewById<Button>(R.id.btnInverse)
        val colorRes = if (isInverseMode) R.color.inverse_mode_active else R.color.scientific_button
        inverseBtn.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
    }

    /** Calcula o resultado da expressão atual. */
    private fun calculateResult() {
        if (currentInput.isBlank() || currentInput == "Erro") {
            currentInput = "0"
            currentExpression = ""
            updateDisplay()
            return
        }
        try {
            var expressionToEvaluate = currentInput
            val openParenCount = expressionToEvaluate.count { it == '(' }
            val closeParenCount = expressionToEvaluate.count { it == ')' }
            if (openParenCount > closeParenCount) {
                expressionToEvaluate += ")".repeat(openParenCount - closeParenCount)
            }

            currentExpression = expressionToEvaluate
            val result = evaluateExpression(expressionToEvaluate)
            currentInput = formatResult(result)
        } catch (e: ArithmeticException) {
            currentInput = "Erro (Div/0)"
            Log.e(TAG, "ArithmeticException: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            currentInput = "Erro (Entrada)"
            Log.e(TAG, "IllegalArgumentException: ${e.message}", e)
        } catch (e: Exception) {
            currentInput = "Erro"
            Log.e(TAG, "Exception: ${e.message}", e)
        }
        updateDisplay()
    }

    /** Avalia a expressão matemática usando exp4j. */
    private fun evaluateExpression(expressionStr: String): Double {
        var exprForEval = expressionStr
            .replace("π", "(${Math.PI})")
            .replace("e", "(${Math.E})")
            .replace("√", "sqrt")
            .replace("ln(", "log(")

        // Converte N! para fact(N) para o exp4j
        val factorialFunctionName = "fact"
        val numberFactorialPattern = Regex("(\\d+(?:\\.\\d+)?)\\s*!")
        val parenFactorialPattern = Regex("(\\((?:[^()]|\\([^()]\\))\\))\\s*!")
        var changedInLoop: Boolean
        do {
            changedInLoop = false
            exprForEval = parenFactorialPattern.replace(exprForEval) { matchResult ->
                changedInLoop = true
                "$factorialFunctionName${matchResult.groupValues[1]}"
            }
            exprForEval = numberFactorialPattern.replace(exprForEval) { matchResult ->
                changedInLoop = true
                "$factorialFunctionName(${matchResult.groupValues[1]})"
            }
        } while (changedInLoop)
        // Converte ângulos para radianos se estiver em modo GRAUS.
        if (!isRadiansMode) {
            listOf("sin", "cos", "tan").forEach { func ->
                exprForEval = Regex("\\b$func\\(").replace(exprForEval, "$func((${Math.PI}/180)*")
            }
            listOf("asin", "acos", "atan").forEach { func ->
                exprForEval = Regex("\\b$func\\(").replace(exprForEval, "(180/(${Math.PI}))*$func(")
            }
        }
        Log.d(TAG, "Expressão para exp4j: $exprForEval")

        val factorial = object : Function(factorialFunctionName, 1) {
            override fun apply(vararg args: Double): Double {
                if (args.isEmpty()) throw IllegalArgumentException("Função fatorial (fact) requer 1 argumento.")
                val n = args[0]
                if (n < 0 || n != Math.floor(n) || n > 170) return Double.NaN
                if (n == 0.0) return 1.0
                var result = 1.0
                for (i in 1..n.toInt()) result *= i.toDouble()
                return result
            }
        }
        return ExpressionBuilder(exprForEval).function(factorial).build().evaluate()
    }

    /** Formata o resultado numérico para exibição. */
    private fun formatResult(value: Double): String {
        return when {
            value.isNaN() -> "Erro (Indef.)"
            value.isInfinite() -> "Erro (Infinito)"
            // CORREÇÃO: String.formatLocale -> String.format(Locale, ...)
            Math.abs(value - value.toLong()) < 1e-9 && value.toString().length < 18 ->
                value.toLong().toString().take(MAX_INPUT_LENGTH)
            else -> String.format(Locale.US, "%.8f", value) // Usar Locale.US para garantir ponto decimal.
                .trimEnd('0').trimEnd('.').take(MAX_INPUT_LENGTH)
        }
    }

    /** Atualiza os TextViews da expressão e do resultado. */
    private fun updateDisplay() {
        tvExpression.text = currentExpression
        tvResult.text = currentInput
    }

    /** Exibe uma mensagem Toast curta. */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /** Verifica se um operador ou parêntese pode ser adicionado à entrada. */
    private fun canAddOperatorOrParenthesis(op: String): Boolean {
        val trimmedInput = currentInput.trim()
        if (trimmedInput.length >= MAX_INPUT_LENGTH && op != ")") {
            // showToast("Limite de caracteres atingido") // Removido para não ser repetitivo
            return false
        }
        val lastChar = if (trimmedInput.isEmpty() || (trimmedInput == "0" && op != "(" && op != "-") ) ' ' else trimmedInput.last()

        return when (op) {
            "(" -> trimmedInput == "0" || trimmedInput.isEmpty() || "+-*/%^(".contains(lastChar)
            ")" -> {
                val openParenCount = trimmedInput.count { it == '(' }
                val closeParenCount = trimmedInput.count { it == ')' }
                openParenCount > closeParenCount && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')
            }
            "+", "*", "/", "%", "^" -> trimmedInput.isNotEmpty() && trimmedInput != "0" && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')
            "-" -> trimmedInput == "0" || trimmedInput.isEmpty() || "(".contains(lastChar) || lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || "+-/%^".contains(lastChar) // Permite "5-2"
            else -> true
        }
    }

    /** Anexa uma constante (π ou e) à entrada, com possível "*" implícito. */
    private fun appendConstant(constantValue: String, displayName: String) {
        var preliminaryInput = currentInput
        var addedMultiplication = false

        if (preliminaryInput == "0") {
            preliminaryInput = ""
        } else {
            val lastChar = preliminaryInput.lastOrNull()
            if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
                if ((preliminaryInput + "*").length < MAX_INPUT_LENGTH) {
                    preliminaryInput += "*"
                    addedMultiplication = true
                } else { showToast("Limite de caracteres"); return }
            }
        }
        if (!checkLengthAndShowToast(preliminaryInput, displayName, addedMultiplication)) return
        currentInput = preliminaryInput + displayName
        updateDisplay()
    }

    /** Anexa um operador postfix (ex: !, ^, 10^) ou infix (^) à entrada. */
    private fun appendOperatorOrPostfix(op: String) {
        if (!checkLengthAndShowToast(currentInput, op)) return
        val lastChar = if (currentInput.isEmpty()) ' ' else currentInput.last()

        if (op == "!") {
            if (!(currentInput == "0" || currentInput.isEmpty() || "+-*/%^( ".contains(lastChar))) {
                currentInput += op
            } else if (currentInput == "0" && op == "!") { // Permite 0!
                currentInput += op
            } else { showToast("Entrada inválida para '$op'"); return }
        } else if (op == "^" || op == "10^" || op == "^2") {
            if (currentInput.isNotEmpty() && currentInput != "0" && !"+-*/%^( ".contains(lastChar)) {
                currentInput += op
            } else { showToast("Entrada inválida para '$op'"); return }
        } else { // Caso genérico, se algum outro operador vier por aqui
            currentInput += op
        }
        updateDisplay()
    }

    /** Anexa uma chamada de função (ex: sin(), sqrt()) à entrada, com "*" implícito. */
    private fun appendFunctionCall(funcName: String) {
        var preliminaryInput = currentInput
        var addedMultiplication = false

        if (preliminaryInput == "0" && funcName.endsWith("(")) {
            preliminaryInput = ""
        } else {
            val lastChar = preliminaryInput.lastOrNull()
            if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
                if ((preliminaryInput + "*").length < MAX_INPUT_LENGTH) {
                    preliminaryInput += "*"
                    addedMultiplication = true
                } else { showToast("Limite de caracteres"); return }
            }
        }
        if (!checkLengthAndShowToast(preliminaryInput, funcName, addedMultiplication)) return
        currentInput = preliminaryInput + funcName
        updateDisplay()
    }

    /** Função auxiliar para verificar o comprimento antes de anexar strings. */
    private fun checkLengthAndShowToast(base: String, toAppend: String, hadImplicitOp: Boolean = false): Boolean {
        if ((base + toAppend).length > MAX_INPUT_LENGTH) {
            if (hadImplicitOp) { // Tenta remover o operador implícito se o comprimento for excedido
                val baseWithoutOp = base.removeSuffix("*")
                if ((baseWithoutOp + toAppend).length <= MAX_INPUT_LENGTH) {
                    currentInput = baseWithoutOp // Atualiza currentInput para a versão sem "*"
                    return true // Permite a operação, mas currentInput já foi modificado.
                }
            }
            showToast("Limite de caracteres atingido")
            return false
        }
        return true
    }


    /** Retorna a string da função/operador inverso. */
    private fun getInverseFunction(funcKey: String): String {
        return when (funcKey) {
            "sin(" -> "asin("
            "cos(" -> "acos("
            "tan(" -> "atan("
            "ln(" -> "exp("
            "log10(" -> "10^"
            "sqrt(" -> "^2"
            "^" -> "^(1/(" // Guia para raiz n-ésima: x^(1/y)
            else -> funcKey
        }
    }
}