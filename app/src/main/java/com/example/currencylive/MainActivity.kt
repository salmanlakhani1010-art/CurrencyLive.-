package com.example.currencylive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CurrencyLiveApp()
        }
    }
}

@Composable
fun CurrencyLiveApp() {

    var rates by remember {
        mutableStateOf<Map<String, Double>>(emptyMap())
    }

    var updated by remember {
        mutableStateOf("Not updated yet")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var amount by remember {
        mutableStateOf("1")
    }

    var from by remember {
        mutableStateOf("USD")
    }

    var to by remember {
        mutableStateOf("ZAR")
    }

    var refreshToken by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(refreshToken) {

        loading = true
        error = null

        try {

            val json = withContext(Dispatchers.IO) {
                URL(
                    "https://api.frankfurter.app/latest?from=USD"
                ).readText()
            }

            val obj = JSONObject(json)

            val newRates = mutableMapOf<String, Double>()

            newRates["USD"] = 1.0

            val jsonRates = obj.getJSONObject("rates")

            jsonRates.keys().forEach { currency ->
                newRates[currency] =
                    jsonRates.getDouble(currency)
            }

            rates = newRates

            updated =
                obj.optString(
                    "date",
                    "Latest available"
                )

        } catch (e: Exception) {

            error =
                "Unable to update rates. Check your internet connection."

        } finally {

            loading = false
        }
    }

    fun convert(
        value: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double? {

        val fromRate = rates[fromCurrency]
            ?: return null

        val toRate = rates[toCurrency]
            ?: return null

        return value / fromRate * toRate
    }

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F9FC)
        ) {

            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                item {

                    Text(
                        text = "Currency Live",
                        style =
                            MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Live exchange rates",
                        color = Color.Gray
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Button(
                        onClick = {
                            refreshToken++
                        },
                        enabled = !loading
                    ) {

                        Text(
                            if (loading)
                                "Updating..."
                            else
                                "Refresh rates"
                        )
                    }
                }

                item {

                    Card {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = "Live Rates",
                                style =
                                    MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            RateRow(
                                "USD",
                                "ZAR",
                                convert(1.0, "USD", "ZAR")
                            )

                            RateRow(
                                "USD",
                                "INR",
                                convert(1.0, "USD", "INR")
                            )

                            RateRow(
                                "ZAR",
                                "INR",
                                convert(1.0, "ZAR", "INR")
                            )

                            RateRow(
                                "INR",
                                "ZAR",
                                convert(1.0, "INR", "ZAR")
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(5.dp)
                            )

                            Text(
                                text = "Updated: $updated",
                                color = Color.Gray
                            )

                            if (error != null) {

                                Text(
                                    text = error!!,
                                    color =
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {

                    Card {

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = "Currency Converter",
                                style =
                                    MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = amount,
                                onValueChange = {
                                    amount = it
                                },
                                label = {
                                    Text("Amount")
                                },
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType =
                                            KeyboardType.Decimal
                                    ),
                                modifier =
                                    Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {

                                OutlinedButton(
                                    onClick = {

                                        from =
                                            when (from) {
                                                "USD" -> "ZAR"
                                                "ZAR" -> "INR"
                                                else -> "USD"
                                            }

                                    },
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text("From: $from")
                                }

                                OutlinedButton(
                                    onClick = {

                                        to =
                                            when (to) {
                                                "ZAR" -> "INR"
                                                "INR" -> "USD"
                                                else -> "ZAR"
                                            }

                                    },
                                    modifier =
                                        Modifier.weight(1f)
                                ) {

                                    Text("To: $to")
                                }
                            }

                            val result =
                                convert(
                                    amount.toDoubleOrNull()
                                        ?: 0.0,
                                    from,
                                    to
                                )

                            Text(
                                text =
                                    if (result != null) {
                                        "$amount $from = " +
                                            "%.2f".format(result) +
                                            " $to"
                                    } else {
                                        "Waiting for live rate..."
                                    },
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RateRow(
    from: String,
    to: String,
    rate: Double?
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = "$from → $to",
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text =
                rate?.let {
                    "%.4f".format(it)
                } ?: "—"
        )
    }
}
