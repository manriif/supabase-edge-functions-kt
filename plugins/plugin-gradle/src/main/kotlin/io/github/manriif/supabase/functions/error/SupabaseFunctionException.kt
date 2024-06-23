package io.github.manriif.supabase.functions.error

abstract class SupabaseFunctionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class SupabaseFunctionDeployException(message: String) : SupabaseFunctionException(message)

class SupabaseFunctionServeException(message: String) : SupabaseFunctionException(message)

class SupabaseFunctionImportMapTemplateException(message: String, cause: Throwable?) :
    SupabaseFunctionException(message, cause)

class SupabaseFunctionRequestConfigException(message: String, cause: Throwable?) :
    SupabaseFunctionException(message, cause)