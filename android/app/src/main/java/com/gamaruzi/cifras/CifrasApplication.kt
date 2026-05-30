package com.gamaruzi.cifras

import android.app.Application

class CifrasApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Sem inicialização de SDKs externos por design (ver docs/seguranca-e-custos.md).
    }
}
