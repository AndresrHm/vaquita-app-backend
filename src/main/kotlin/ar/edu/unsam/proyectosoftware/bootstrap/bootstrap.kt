package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.PartesIguales
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import ar.edu.unsam.proyectosoftware.service.GastoService
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
            listOf(agustinH, juanM, andresH, giulianaM, andresS).forEach { it ->
                agregarIntegrante(
                    it,
                    RolIntegrante.MIEMBRO
                )
            }
            agregarIntegrante(juanA, RolIntegrante.ADMIN)
        }

        val grupoDepto = Grupo(
            nombre = "Depto Palermo",
            descripcion = "Gastos de convivencia",
            imagenUrl = "https://definicion.de/wp-content/uploads/2009/02/departamento-1.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(agustinH, juanM, giulianaM).forEach { it ->
                agregarIntegrante(
                    it,
                    RolIntegrante.MIEMBRO
                )
            }
            agregarIntegrante(andresS, RolIntegrante.ADMIN)
        }

        val grupoFutbol = Grupo(
            nombre = "Los Pibes FC",
            descripcion = "Canchita los martes",
            imagenUrl = "https://www.historiadeboca.com.ar/Fotos/Equipos/2000301.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(
                juanM, andresH, juanA
            ).forEach { it ->
                agregarIntegrante(
                    it,
                    RolIntegrante.MIEMBRO
                )
            }
            agregarIntegrante(giulianaM, RolIntegrante.ADMIN)
        }

        val grupoEstudio = Grupo(
            nombre = "Final de Objetos",
            descripcion = "Café y apuntes",
            imagenUrl = "https://www.pcgamesn.com/wp-content/sites/pcgamesn/2019/06/minecraft-enchantments-enchanting-table.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(giulianaM, juanA, andresS).forEach { it -> agregarIntegrante(it, RolIntegrante.MIEMBRO) }
            agregarIntegrante(andresS, RolIntegrante.ADMIN)
        }

        grupoRepository.saveAll(listOf(grupoAsado, grupoDepto, grupoFutbol, grupoEstudio))

        // 3. Gastos
        listOf(
            PartesIguales(
                "Algo para picar",
                CategoriaGasto.COMIDA,
                60000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Picada completa", 1.0, 60000.0) },
            PartesIguales(
                "Compras para el asado",
                CategoriaGasto.COMIDA,
                15000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios(),
            ).apply {
                agregarItem("Vacío", 2.5, 4000.0); agregarItem("Carbón", 1.0, 2000.0); agregarItem(
                "Fernet",
                1.0,
                3000.0
            )
            },
            PartesIguales(
                "Bebidas previa",
                CategoriaGasto.BEBIDA,
                12000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios(),
            ).apply { agregarItem("Cerveza", 6.0, 1500.0); agregarItem("Gaseosa", 2.0, 1500.0) },
            PartesIguales(
                "Panadería",
                CategoriaGasto.COMIDA,
                4500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Mignon", 2.0, 1500.0); agregarItem("Facturas", 1.0, 1500.0) },
            PartesIguales(
                "Verdulería",
                CategoriaGasto.COMIDA,
                5800.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply {
                agregarItem("Papa", 3.0, 1000.0); agregarItem("Tomate", 1.0, 2000.0); agregarItem(
                "Lechuga",
                1.0,
                800.0
            )
            },
            PartesIguales(
                "Más Carne",
                CategoriaGasto.COMIDA,
                18000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Chorizos", 12.0, 1000.0); agregarItem("Morcilla", 6.0, 1000.0) },
            PartesIguales(
                "Hielo y Carbón extra",
                CategoriaGasto.VARIOS,
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Hielo", 3.0, 1000.0); agregarItem("Carbón", 1.0, 1000.0) },
            PartesIguales(
                "Picada",
                CategoriaGasto.COMIDA,
                9500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Salame", 1.0, 4500.0); agregarItem("Queso", 1.0, 5000.0) },
            PartesIguales(
                "Snacks",
                CategoriaGasto.COMIDA,
                3200.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Papas fritas", 2.0, 1600.0) },
            PartesIguales(
                "Postre Helado",
                CategoriaGasto.COMIDA,
                8000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Bombón Suizo", 8.0, 1000.0) },
            PartesIguales(
                "Vino",
                CategoriaGasto.BEBIDA,
                11000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Malbec", 2.0, 5500.0) },
            PartesIguales(
                "Aderezos y servilletas",
                CategoriaGasto.VARIOS,
                2500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Mayonesa", 1.0, 1000.0); agregarItem("Servilletas", 2.0, 750.0) },
            PartesIguales(
                "Achuras",
                CategoriaGasto.COMIDA,
                7000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Chinchulines", 1.0, 4000.0); agregarItem("Riñones", 1.0, 3000.0) },
            PartesIguales(
                "Cerveza extra",
                CategoriaGasto.BEBIDA,
                9000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Latas", 6.0, 1500.0) },
            PartesIguales(
                "Postre Vigilante",
                CategoriaGasto.COMIDA,
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Dulce de membrillo", 1.0, 2000.0); agregarItem("Queso fresco", 1.0, 2000.0) },
            PartesIguales(
                "Soda",
                CategoriaGasto.BEBIDA,
                2400.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Sifón", 3.0, 800.0) },
            PartesIguales(
                "Leña",
                CategoriaGasto.VARIOS,
                3000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Bolsa leña", 2.0, 1500.0) },
            PartesIguales(
                "Provoleta",
                CategoriaGasto.COMIDA,
                5000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Provoleta unidad", 2.0, 2500.0) },
            PartesIguales(
                "Fernet de refuerzo",
                CategoriaGasto.BEBIDA,
                7500.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Fernet 750ml", 1.0, 7500.0) },
            PartesIguales(
                "Limones y Sal",
                CategoriaGasto.VARIOS,
                1800.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Limón", 1.0, 1000.0); agregarItem("Sal gruesa", 1.0, 800.0) },
            PartesIguales(
                "Gaseosas Dieta",
                CategoriaGasto.BEBIDA,
                4600.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Pepsi Black", 2.0, 2300.0) },
            PartesIguales(
                "Carne de Cerdo",
                CategoriaGasto.COMIDA,
                12000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Pechito", 1.5, 8000.0); agregarItem("Manta", 1.0, 4000.0) },
            PartesIguales(
                "Chimichurri ingredientes",
                CategoriaGasto.VARIOS,
                2200.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Aji molido", 1.0, 1100.0); agregarItem("Aceite", 1.0, 1100.0) },
            PartesIguales(
                "Vaso Descartable",
                CategoriaGasto.VARIOS,
                3500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Paquete vasos", 1.0, 3500.0) }
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            // Lógica de filtrado de deudas propias
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)
        }
    }
}
