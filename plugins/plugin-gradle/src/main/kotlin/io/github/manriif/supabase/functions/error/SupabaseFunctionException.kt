package io.github.manriif.supabase.functions.error

internal abstract class SupabaseFunctionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

internal class SupabaseFunctionDeployException(message: String) : SupabaseFunctionException(message)

internal class SupabaseFunctionServeException(message: String) : SupabaseFunctionException(message)

internal class SupabaseFunctionImportMapTemplateException(message: String, cause: Throwable?) :
    SupabaseFunctionException(message, cause)

internal class SupabaseFunctionRequestConfigException(message: String, cause: Throwable?) :
    SupabaseFunctionException(message, cause)