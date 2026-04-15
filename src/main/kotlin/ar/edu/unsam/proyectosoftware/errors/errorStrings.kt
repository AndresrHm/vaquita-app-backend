package ar.edu.unsam.proyectosoftware.errors

object ErrorStrings {
    //Validaciones básicas
    const val MONTO_NEGATIVO = "El monto del gasto debe ser mayor a cero."
    const val MONTO_TOTAL_NO_COINCIDE_CON_TOTAL_ITEMS =
        "El total del monto del gasto debe coincidir con la suma de los ítems del mismo"
    const val DESCRIPCION_VACIA = "La descripción no puede estar vacía."
    const val INFORMACION_INVALIDA = "La información ingresada es inválida."
    const val NOMBRE_OBLIGATORIO = "El nombre es obligatorio."
    fun DESCRIPCION_LARGA(maxCaracteres: Int): String {
        return "La descripción no puede tener más de ${maxCaracteres} caracteres."
    }
    fun NOMBRE_PRODUCTO_LARGO(maxCaracteres: Int): String {
        return "Los nombres de los productos no pueden tener más de ${maxCaracteres} caracteres."
    }

    //Participantes de grupos
    const val PAGADOR_NO_PERTENECE_AL_GRUPO = "El usuario pagador no pertenece al grupo seleccionado."
    const val PARTICIPANTES_NO_PERTENECEN_AL_GRUPO =
        "Todos los usuarios participantes deben pertenecer al grupo correspondiente"
    const val USUARIO_YA_PERTENECE_AL_GRUPO = "El usuario ya pertenece al grupo."
    const val USUARIO_NO_PERTENECE_AL_GRUPO = "El usuario no pertenece al grupo seleccionado"

    fun USUARIO_NO_PERTENECE_A_PARTICIPANTES(idUsuario: Long): String {
        return "El usuario con ID $idUsuario no pertenece a los participantes del gasto"
    }

    //Permisos
    const val USUARIO_SIN_PERMISO_GASTO = "El usuario no tiene permisos para registrar gastos en este grupo."
    const val USUARIO_NO_ES_ADMIN = "El usuario no es administrador del grupo."
    const val NO_PODES_ELIMINARTE = "No podés eliminarte a vos mismo."
    const val NO_SE_PUEDE_ELIMINAR_ULTIMO_ADMIN = "No podés eliminar al último administrador."

    //Entidades no encontradas
    const val USUARIO_NO_ENCONTRADO = "El usuario no ha sido encontrado."
    const val GRUPO_NO_ENCONTRADO = "El grupo no ha sido encontrado."
    const val NINGUN_USUARIO_ENCONTRADO = "Uno o más elementos no fueron encontrados"

    //Pagos
    const val FRACCION_YA_SALDADA = "Esta deuda ya se encuentra cancelada"
    const val PAGO_MAYOR_A_DEUDA = "No se puede cancelar un monto mayor a la deuda"
    const val PAGO_NEGATIVO = "Debe pagar un monto positivo"

    //Asignaciones
    const val CANTIDAD_PARTICIPANTES_NO_COINCIDE_CANTIDAD_ASIGNACIONES =
        "La cantidad de asignaciones no coincide con la cantidad de participantes"
    const val FALTAN_ASIGNACIONES = "Faltan las asignaciones para el tipo PARTES_EXACTAS"
    const val FRACCIONES_NO_COINCIDEN_CON_TOTAL_DE_GASTO = "La suma de los montos no coincide con el total del gasto"
    const val ASIGNACIONES_DEBEN_TENER_MONTO_POSITIVO = "Todas las asignaciones exactas deben tener un monto positivo"

}