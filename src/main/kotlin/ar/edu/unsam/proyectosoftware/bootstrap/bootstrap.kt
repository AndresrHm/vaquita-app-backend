package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.domain.TipoGasto
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.boot.CommandLineRunner
import java.time.LocalDate

@Configuration
class BootstrapData(
    val usuarioRepository: UsuarioRepository, val grupoRepository: GrupoRepository, val gastoRepository: GastoRepository
) {

    @Bean
    fun initData() = CommandLineRunner {
        // 1. Usuarios
        val juanA = Usuario().apply { nombre = "Juan Alvarez" }
        val andresH = Usuario().apply { nombre = "Andrés Hernández" }
        val agustinH = Usuario().apply { nombre = "Agustín Hoj" }
        val juanM = Usuario().apply { nombre = "Juan Maidana" }
        val giulianaM = Usuario().apply { nombre = "Giuliana Massanet" }
        val andresS = Usuario().apply { nombre = "Andrés Simonini" }

        usuarioRepository.saveAll(listOf(juanA, agustinH, juanM, andresH, giulianaM, andresS))

        // 2. Grupos con .addAll()
        val grupoAsado = Grupo(
            nombre = "Asado UNSAM",
            descripcion = "Asadito en el campus",
            imagenUrl = "https://i.ytimg.com/vi/OWt0hpRwqSs/hqdefault.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(juanA, agustinH, andresH, andresS)) }

        val grupoDepto = Grupo(
            nombre = "Depto Palermo",
            descripcion = "Gastos de convivencia",
            imagenUrl = "https://definicion.de/wp-content/uploads/2009/02/departamento-1.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(agustinH, juanM, giulianaM)) }

        val grupoFutbol = Grupo(
            nombre = "Los Pibes FC",
            descripcion = "Canchita los martes",
            imagenUrl = "https://www.historiadeboca.com.ar/Fotos/Equipos/2000301.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(juanM, andresH, juanA)) }

        val grupoEstudio = Grupo(
            nombre = "Final de Objetos",
            descripcion = "Café y apuntes",
            imagenUrl = "https://www.pcgamesn.com/wp-content/sites/pcgamesn/2019/06/minecraft-enchantments-enchanting-table.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(giulianaM, juanA, agustinH, andresS)) }

        grupoRepository.saveAll(listOf(grupoAsado, grupoDepto, grupoFutbol, grupoEstudio))

        // 3. Un gasto de prueba en el Grupo 1
        val gasto1 = Gasto(
            descripcion = "Compras para el asado",
            categoria = "COMIDA",
            monto = 15000.0,
            fecha = LocalDate.now(),
            grupo = grupoAsado,
            pagador = juanA,
            participantes = grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )

        val gasto2 = Gasto(
            "Bebidas previa",
            "BEBIDA",
            12000.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto2.agregarItem("Cerveza", 6.0, 1500.0)
        gasto2.agregarItem("Gaseosa", 2.0, 1500.0)
        gastoRepository.save(gasto2)

        val gasto3 = Gasto(
            "Panadería",
            "COMIDA",
            4500.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto3.agregarItem("Mignon", 2.0, 1500.0)
        gasto3.agregarItem("Facturas", 1.0, 1500.0)
        gastoRepository.save(gasto3)

        val gasto4 = Gasto(
            "Verdulería",
            "COMIDA",
            5800.0,
            LocalDate.now(),
            grupoAsado,
            andresS,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto4.agregarItem("Papa", 3.0, 1000.0)
        gasto4.agregarItem("Tomate", 1.0, 2000.0)
        gasto4.agregarItem("Lechuga", 1.0, 800.0)
        gastoRepository.save(gasto4)

        val gasto5 = Gasto(
            "Más Carne",
            "COMIDA",
            18000.0,
            LocalDate.now(),
            grupoAsado,
            juanA,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto5.agregarItem("Chorizos", 12.0, 1000.0)
        gasto5.agregarItem("Morcilla", 6.0, 1000.0)
        gastoRepository.save(gasto5)

        val gasto6 = Gasto(
            "Hielo y Carbón extra",
            "VARIOS",
            4000.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto6.agregarItem("Hielo", 3.0, 1000.0)
        gasto6.agregarItem("Carbón", 1.0, 1000.0)
        gastoRepository.save(gasto6)

        val gasto7 = Gasto(
            "Picada",
            "COMIDA",
            9500.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto7.agregarItem("Salame", 1.0, 4500.0)
        gasto7.agregarItem("Queso", 1.0, 5000.0)
        gastoRepository.save(gasto7)

        val gasto8 = Gasto(
            "Snacks",
            "COMIDA",
            3200.0,
            LocalDate.now(),
            grupoAsado,
            andresS,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto8.agregarItem("Papas fritas", 2.0, 1600.0)
        gastoRepository.save(gasto8)

        val gasto9 = Gasto(
            "Postre Helado",
            "COMIDA",
            8000.0,
            LocalDate.now(),
            grupoAsado,
            juanA,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto9.agregarItem("Bombón Suizo", 8.0, 1000.0)
        gastoRepository.save(gasto9)

        val gasto10 = Gasto(
            "Vino",
            "BEBIDA",
            11000.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto10.agregarItem("Malbec", 2.0, 5500.0)
        gastoRepository.save(gasto10)

        val gasto11 = Gasto(
            "Aderezos y servilletas",
            "VARIOS",
            2500.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto11.agregarItem("Mayonesa", 1.0, 1000.0)
        gasto11.agregarItem("Servilletas", 2.0, 750.0)
        gastoRepository.save(gasto11)

        val gasto12 = Gasto(
            "Achuras",
            "COMIDA",
            7000.0,
            LocalDate.now(),
            grupoAsado,
            andresS,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto12.agregarItem("Chinchulines", 1.0, 4000.0)
        gasto12.agregarItem("Riñones", 1.0, 3000.0)
        gastoRepository.save(gasto12)

        val gasto13 = Gasto(
            "Cerveza extra",
            "BEBIDA",
            9000.0,
            LocalDate.now(),
            grupoAsado,
            juanA,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto13.agregarItem("Latas", 6.0, 1500.0)
        gastoRepository.save(gasto13)

        val gasto14 = Gasto(
            "Postre Vigilante",
            "COMIDA",
            4000.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto14.agregarItem("Dulce de membrillo", 1.0, 2000.0)
        gasto14.agregarItem("Queso fresco", 1.0, 2000.0)
        gastoRepository.save(gasto14)

        val gasto15 = Gasto(
            "Soda",
            "BEBIDA",
            2400.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto15.agregarItem("Sifón", 3.0, 800.0)
        gastoRepository.save(gasto15)

        val gasto16 = Gasto(
            "Leña",
            "VARIOS",
            3000.0,
            LocalDate.now(),
            grupoAsado,
            andresS,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto16.agregarItem("Bolsa leña", 2.0, 1500.0)
        gastoRepository.save(gasto16)

        val gasto17 = Gasto(
            "Provoleta",
            "COMIDA",
            5000.0,
            LocalDate.now(),
            grupoAsado,
            juanA,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto17.agregarItem("Provoleta unidad", 2.0, 2500.0)
        gastoRepository.save(gasto17)

        val gasto18 = Gasto(
            "Fernet de refuerzo",
            "BEBIDA",
            7500.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto18.agregarItem("Fernet 750ml", 1.0, 7500.0)
        gastoRepository.save(gasto18)

        val gasto19 = Gasto(
            "Limones y Sal",
            "VARIOS",
            1800.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto19.agregarItem("Limón", 1.0, 1000.0)
        gasto19.agregarItem("Sal gruesa", 1.0, 800.0)
        gastoRepository.save(gasto19)

        val gasto20 = Gasto(
            "Gaseosas Dieta",
            "BEBIDA",
            4600.0,
            LocalDate.now(),
            grupoAsado,
            andresS,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto20.agregarItem("Pepsi Black", 2.0, 2300.0)
        gastoRepository.save(gasto20)

        val gasto21 = Gasto(
            "Carne de Cerdo",
            "COMIDA",
            12000.0,
            LocalDate.now(),
            grupoAsado,
            juanA,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto21.agregarItem("Pechito", 1.5, 8000.0)
        gasto21.agregarItem("Manta", 1.0, 4000.0)
        gastoRepository.save(gasto21)

        val gasto22 = Gasto(
            "Chimichurri ingredientes",
            "VARIOS",
            2200.0,
            LocalDate.now(),
            grupoAsado,
            agustinH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto22.agregarItem("Aji molido", 1.0, 1100.0)
        gasto22.agregarItem("Aceite", 1.0, 1100.0)
        gastoRepository.save(gasto22)

        val gasto23 = Gasto(
            "Vaso Descartable",
            "VARIOS",
            3500.0,
            LocalDate.now(),
            grupoAsado,
            andresH,
            grupoAsado.integrantes.toMutableList(),
            tipoGasto = TipoGasto.PARTES_IGUALES
        )
        gasto23.agregarItem("Paquete vasos", 1.0, 3500.0)
        gastoRepository.save(gasto23)

        gasto1.agregarItem("Vacío", 2.5, 4000.0)
        gasto1.agregarItem("Carbón", 1.0, 2000.0)
        gasto1.agregarItem("Fernet", 1.0, 3000.0)

        gastoRepository.saveAll(
            listOf(
                gasto1,
                gasto2,
                gasto3,
                gasto4,
                gasto5,
                gasto6,
                gasto7,
                gasto8,
                gasto9,
                gasto10,
                gasto11,
                gasto12,
                gasto13,
                gasto14,
                gasto15,
                gasto16,
                gasto17,
                gasto18,
                gasto19,
                gasto20,
                gasto21,
                gasto22,
                gasto23
            )
        )

    }
}