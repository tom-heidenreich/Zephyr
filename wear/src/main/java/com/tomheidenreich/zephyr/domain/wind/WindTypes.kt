package com.tomheidenreich.zephyr.domain.wind

enum class WindReference {
    TRUE,
    APPARENT,
}

enum class WindDataSourceType {
    API,
    INSTRUMENT,
    FUSED,
}