package com.grup.di

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.core.context.startKoin

actual fun initKoin(appDeclaration: (KoinApplication.() -> Unit)?) {
    startKoin {
        appDeclaration?.let { it() }
        loadKoinModules(
            module {
                single {
                    HttpClient(Darwin) {
                        install(ContentNegotiation) {
                            json(
                                Json {
                                    ignoreUnknownKeys = true
                                    isLenient = true
                                    prettyPrint = true
                                },
                                contentType = ContentType.Application.Json
                            )
                        }
                    }
                }
            }
        )
    }
}