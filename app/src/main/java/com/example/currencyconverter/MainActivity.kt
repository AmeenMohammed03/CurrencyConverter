package com.example.currencyconverter

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.*
import kotlin.collections.HashMap


interface ConversionRatesApi {
    @GET("content.json")
    suspend fun getRates(): ConversionRatesResponse
}

data class ConversionRatesResponse(val rates: Map<String, Double>)

class MainActivity : AppCompatActivity() {

    private lateinit var editTextAmount: EditText
    private lateinit var spinnerCountry: Spinner
    private lateinit var listViewConversion: ListView

    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var conversionAdapter: ArrayAdapter<String>

    private var selectedCountry: String? = null
    private var conversionRates: HashMap<String, Double>? = null

    private val conversionRatesApi: ConversionRatesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://dqcyzkkq84hco.cloudfront.net/content.json")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConversionRatesApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextAmount = findViewById(R.id.amountEditText)
        spinnerCountry = findViewById(R.id.spinnerCountry)

        countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = countryAdapter

        conversionAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listViewConversion.adapter = conversionAdapter

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { conversionRatesApi.getRates() }
                val rates = response.rates
                conversionRates = HashMap(rates)
                for (country in conversionRates?.keys!!) {
                    countryAdapter.add(country)
                }
                countryAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to fetch conversion rates", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        findViewById<Button>(R.id.convertButton).setOnClickListener {
            convert()
        }
    }

    private fun convert() {
        val selectedCountry = spinnerCountry.selectedItem as String

        val amountStr = editTextAmount.text.toString()

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()

        if (amount == null || amount == 0.0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCountry.isEmpty()) {
            Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
            return
        }

        conversionAdapter.clear()

        for (country in conversionRates?.keys!!) {
            val rate = conversionRates?.get(country)
            val convertedAmount = amount * rate!!

            val result = String.format(Locale.getDefault(), "%s %.2f", country, convertedAmount)
            conversionAdapter.add(result)
        }
    }
}