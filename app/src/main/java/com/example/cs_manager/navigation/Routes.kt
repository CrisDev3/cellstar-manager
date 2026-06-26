package com.example.cs_manager.navigation

/**
 * Rutas centralizadas de navegación de la aplicación CellStar Manager.
 */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home/{username}"
    const val INVENTORY = "inventory"
    const val REPORTS = "reports"
    const val ALERTS = "alerts"
    const val SALES = "sales"
    const val ADD_PRODUCT = "add_product"

    /**
     * Construye la ruta de Home con el argumento de nombre de usuario correspondiente.
     */
    fun createHomeRoute(username: String): String {
        return "home/$username"
    }
}
