package com.ablomm.monthlygoalmanager.domain.enums

/**
 * Sorting options for goal list display
 */
enum class SortMode {
    /**
     * Default order based on creation/display order
     */
    DEFAULT,
    
    /**
     * Sort by goal priority (High, Middle, Low)
     */
    PRIORITY,
    
    /**
     * Sort by progress percentage (highest first)
     */
    PROGRESS
}
