package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.domain.TipoGasto
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@Configuration
class BootstrapData(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val gastoRepository: GastoRepository,
    val fraccionGastoRepository: FraccionGastoRepository,
    val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData() = CommandLineRunner {
        // 1. Usuarios
        val juanA = Usuario().apply {
            nombre = "Juan Alvarez"
            email = "juan.alvarez@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val andresH = Usuario().apply {
            nombre = "Andrés Hernández"
            email = "andres.h@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val agustinH = Usuario().apply {
            nombre = "Agustín Hoj"
            email = "agustin.hoj@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val juanM = Usuario().apply {
            nombre = "Juan Maidana"
            email = "juan.m@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val giulianaM = Usuario().apply {
            nombre = "Giuliana Massanet"
            email = "giuli.m@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val andresS = Usuario().apply {
            nombre = "Andrés Simonini"
            email = "andres.s@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
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

        // 3. Gastos
        listOf(
            Gasto(
                "Compras para el asado",
                "COMIDA",
                15000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Vacío", 2.5, 4000.0)
                agregarItem("Carbón", 1.0, 2000.0)
                agregarItem("Fernet", 1.0, 3000.0)
            },
            Gasto(
                "Bebidas previa",
                "BEBIDA",
                12000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Cerveza", 6.0, 1500.0)
                agregarItem("Gaseosa", 2.0, 1500.0)
            },
            Gasto(
                "Panadería",
                "COMIDA",
                4500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Mignon", 2.0, 1500.0)
                agregarItem("Facturas", 1.0, 1500.0)
            },
            Gasto(
                "Verdulería",
                "COMIDA",
                5800.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Papa", 3.0, 1000.0)
                agregarItem("Tomate", 1.0, 2000.0)
                agregarItem("Lechuga", 1.0, 800.0)
            },
            Gasto(
                "Más Carne",
                "COMIDA",
                18000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Chorizos", 12.0, 1000.0)
                agregarItem("Morcilla", 6.0, 1000.0)
            },
            Gasto(
                "Hielo y Carbón extra",
                "VARIOS",
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Hielo", 3.0, 1000.0)
                agregarItem("Carbón", 1.0, 1000.0)
            },
            Gasto(
                "Picada",
                "COMIDA",
                9500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Salame", 1.0, 4500.0)
                agregarItem("Queso", 1.0, 5000.0)
            },
            Gasto(
                "Snacks",
                "COMIDA",
                3200.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Papas fritas", 2.0, 1600.0)
            },
            Gasto(
                "Postre Helado",
                "COMIDA",
                8000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Bombón Suizo", 8.0, 1000.0)
            },
            Gasto(
                "Vino",
                "BEBIDA",
                11000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Malbec", 2.0, 5500.0)
            },
            Gasto(
                "Aderezos y servilletas",
                "VARIOS",
                2500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Mayonesa", 1.0, 1000.0)
                agregarItem("Servilletas", 2.0, 750.0)
            },
            Gasto(
                "Achuras",
                "COMIDA",
                7000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Chinchulines", 1.0, 4000.0)
                agregarItem("Riñones", 1.0, 3000.0)
            },
            Gasto(
                "Cerveza extra",
                "BEBIDA",
                9000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Latas", 6.0, 1500.0)
            },
            Gasto(
                "Postre Vigilante",
                "COMIDA",
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Dulce de membrillo", 1.0, 2000.0)
                agregarItem("Queso fresco", 1.0, 2000.0)
            },
            Gasto(
                "Soda",
                "BEBIDA",
                2400.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Sifón", 3.0, 800.0)
            },
            Gasto(
                "Leña",
                "VARIOS",
                3000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Bolsa leña", 2.0, 1500.0)
            },
            Gasto(
                "Provoleta",
                "COMIDA",
                5000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Provoleta unidad", 2.0, 2500.0)
            },
            Gasto(
                "Fernet de refuerzo",
                "BEBIDA",
                7500.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Fernet 750ml", 1.0, 7500.0)
            },
            Gasto(
                "Limones y Sal",
                "VARIOS",
                1800.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Limón", 1.0, 1000.0)
                agregarItem("Sal gruesa", 1.0, 800.0)
            },
            Gasto(
                "Gaseosas Dieta",
                "BEBIDA",
                4600.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Pepsi Black", 2.0, 2300.0)
            },
            Gasto(
                "Carne de Cerdo",
                "COMIDA",
                12000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Pechito", 1.5, 8000.0)
                agregarItem("Manta", 1.0, 4000.0)
            },
            Gasto(
                "Chimichurri ingredientes",
                "VARIOS",
                2200.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Aji molido", 1.0, 1100.0)
                agregarItem("Aceite", 1.0, 1100.0)
            },
            Gasto(
                "Vaso Descartable",
                "VARIOS",
                3500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.integrantes.toMutableList(),
                TipoGasto.PARTES_IGUALES
            ).apply {
                agregarItem("Paquete vasos", 1.0, 3500.0)
            }
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)
        }

    }
}