package io.github.jan.supabase.realtime

sealed class PostgresAction<T> {
    data class Insert<T>(val record: T) : PostgresAction<T>()
    data class Update<T>(val record: T) : PostgresAction<T>()
    data class Delete<T>(val oldRecord: T) : PostgresAction<T>()
}
