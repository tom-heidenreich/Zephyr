package com.tomheidenreich.zephyr.core.result

/**
 * Generic repository state that models loading, data, and error with freshness metadata.
 */
sealed interface RepositoryResult<out T> {
    data object Loading : RepositoryResult<Nothing>

    data class Data<T>(
        val value: T,
        val freshness: DataFreshness = DataFreshness.FRESH,
    ) : RepositoryResult<T>

    data class Error(
        val throwable: Throwable,
        val previousDataFreshness: DataFreshness? = null,
    ) : RepositoryResult<Nothing>
}

enum class DataFreshness {
    FRESH,
    STALE,
}
