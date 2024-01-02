package com.libanux.medicaldashboard_backend.items.models

data class Category(
    val id:Long =0,
    val category: String = ""
) {

    override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Category) return false
            return id == other.id && category == other.category
        }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + category.hashCode()
        return result
    }

}