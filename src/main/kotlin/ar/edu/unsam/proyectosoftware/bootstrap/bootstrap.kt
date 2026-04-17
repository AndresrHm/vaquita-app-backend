package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.PartesIguales
import ar.edu.unsam.proyectosoftware.domain.Usuario
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
        juanA.amigos = mutableListOf(andresH, agustinH)
        andresH.amigos = mutableListOf(agustinH, juanA)
        agustinH.amigos = mutableListOf(andresH, juanA)

        usuarioRepository.saveAll(listOf(juanA, agustinH, juanM, andresH, giulianaM, andresS))

        // 2. Grupos con .addAll()
        val grupoAsado = Grupo(
            nombre = "Asado UNSAM",
            descripcion = "Asadito en el campus",
            imagenUrl = "https://i.ytimg.com/vi/OWt0hpRwqSs/hqdefault.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            integrantes.addAll(listOf(juanA, agustinH, andresH, andresS))
            admins.addAll(listOf(juanA)) // esto es horrible, hay que refactorizar
        }

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
            PartesIguales(
                descripcion = "Compras para el asado",
                categoria = "COMIDA",
                monto = 15000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Vacío", 2.5, 4000.0)
                agregarItem("Carbón", 1.0, 2000.0)
                agregarItem("Fernet", 1.0, 3000.0)
            },
            PartesIguales(
                descripcion = "Bebidas previa",
                categoria = "BEBIDA",
                monto = 12000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Cerveza", 6.0, 1500.0)
                agregarItem("Gaseosa", 2.0, 1500.0)
            },
            PartesIguales(
                descripcion = "Panadería",
                categoria = "COMIDA",
                monto = 4500.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Mignon", 2.0, 1500.0)
                agregarItem("Facturas", 1.0, 1500.0)
            },
            PartesIguales(
                descripcion = "Verdulería",
                categoria = "COMIDA",
                monto = 5800.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresS,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Papa", 3.0, 1000.0)
                agregarItem("Tomate", 1.0, 2000.0)
                agregarItem("Lechuga", 1.0, 800.0)
            },
            PartesIguales(
                descripcion = "Más Carne",
                categoria = "COMIDA",
                monto = 18000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Chorizos", 12.0, 1000.0)
                agregarItem("Morcilla", 6.0, 1000.0)
            },
            PartesIguales(
                descripcion = "Hielo y Carbón extra",
                categoria = "VARIOS",
                monto = 4000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Hielo", 3.0, 1000.0)
                agregarItem("Carbón", 1.0, 1000.0)
            },
            PartesIguales(
                descripcion = "Picada",
                categoria = "COMIDA",
                monto = 9500.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Salame", 1.0, 4500.0)
                agregarItem("Queso", 1.0, 5000.0)
            },
            PartesIguales(
                descripcion = "Snacks",
                categoria = "COMIDA",
                monto = 3200.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresS,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Papas fritas", 2.0, 1600.0)
            },
            PartesIguales(
                descripcion = "Postre Helado",
                categoria = "COMIDA",
                monto = 8000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Bombón Suizo", 8.0, 1000.0)
            },
            PartesIguales(
                descripcion = "Vino",
                categoria = "BEBIDA",
                monto = 11000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Malbec", 2.0, 5500.0)
            },
            PartesIguales(
                descripcion = "Aderezos y servilletas",
                categoria = "VARIOS",
                monto = 2500.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Mayonesa", 1.0, 1000.0)
                agregarItem("Servilletas", 2.0, 750.0)
            },
            PartesIguales(
                descripcion = "Achuras",
                categoria = "COMIDA",
                monto = 7000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresS,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Chinchulines", 1.0, 4000.0)
                agregarItem("Riñones", 1.0, 3000.0)
            },
            PartesIguales(
                descripcion = "Cerveza extra",
                categoria = "BEBIDA",
                monto = 9000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Latas", 6.0, 1500.0)
            },
            PartesIguales(
                descripcion = "Postre Vigilante",
                categoria = "COMIDA",
                monto = 4000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Dulce de membrillo", 1.0, 2000.0)
                agregarItem("Queso fresco", 1.0, 2000.0)
            },
            PartesIguales(
                descripcion = "Soda",
                categoria = "BEBIDA",
                monto = 2400.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Sifón", 3.0, 800.0)
            },
            PartesIguales(
                descripcion = "Leña",
                categoria = "VARIOS",
                monto = 3000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresS,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Bolsa leña", 2.0, 1500.0)
            },
            PartesIguales(
                descripcion = "Provoleta",
                categoria = "COMIDA",
                monto = 5000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Provoleta unidad", 2.0, 2500.0)
            },
            PartesIguales(
                descripcion = "Fernet de refuerzo",
                categoria = "BEBIDA",
                monto = 7500.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Fernet 750ml", 1.0, 7500.0)
            },
            PartesIguales(
                descripcion = "Limones y Sal",
                categoria = "VARIOS",
                monto = 1800.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Limón", 1.0, 1000.0)
                agregarItem("Sal gruesa", 1.0, 800.0)
            },
            PartesIguales(
                descripcion = "Gaseosas Dieta",
                categoria = "BEBIDA",
                monto = 4600.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresS,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Pepsi Black", 2.0, 2300.0)
            },
            PartesIguales(
                descripcion = "Carne de Cerdo",
                categoria = "COMIDA",
                monto = 12000.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = juanA,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Pechito", 1.5, 8000.0)
                agregarItem("Manta", 1.0, 4000.0)
            },
            PartesIguales(
                descripcion = "Chimichurri ingredientes",
                categoria = "VARIOS",
                monto = 2200.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = agustinH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Aji molido", 1.0, 1100.0)
                agregarItem("Aceite", 1.0, 1100.0)
            },
            PartesIguales(
                descripcion = "Vaso Descartable",
                categoria = "VARIOS",
                monto = 3500.0,
                fecha = LocalDate.now(),
                grupo = grupoAsado,
                pagador = andresH,
                participantes = grupoAsado.integrantes.toList()
            ).apply {
                agregarItem("Paquete vasos", 1.0, 3500.0)
            }
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            fraccionGastoRepository.saveAll(fracciones)
        }

    }
}