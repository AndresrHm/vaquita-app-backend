package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.CategoriaGasto
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
        val camilaR = Usuario().apply {
            nombre = "Camila Rojas"
            email = "camila.rojas@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val lucasP = Usuario().apply {
            nombre = "Lucas Pereyra"
            email = "lucas.p@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val valentinaN = Usuario().apply {
            nombre = "Valentina Nuñez"
            email = "valentina.n@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val matiasF = Usuario().apply {
            nombre = "Matías Ferreyra"
            email = "matias.f@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }
        val sofiaL = Usuario().apply {
            nombre = "Sofía López"
            email = "sofia.l@mail.com"
            contrasena = passwordEncoder.encode("contrasena123")
        }

        juanA.amigos = mutableListOf(andresH, agustinH)
        andresH.amigos = mutableListOf(agustinH, juanA, camilaR, matiasF)
        agustinH.amigos = mutableListOf(andresH, juanA, giulianaM, valentinaN)
        juanM.amigos = mutableListOf(giulianaM, matiasF, lucasP)
        giulianaM.amigos = mutableListOf(juanM, agustinH, valentinaN, camilaR)
        andresS.amigos = mutableListOf(sofiaL, camilaR, juanA)
        camilaR.amigos = mutableListOf(andresS, andresH, giulianaM)
        lucasP.amigos = mutableListOf(juanA, juanM, matiasF)
        valentinaN.amigos = mutableListOf(agustinH, giulianaM, sofiaL)
        matiasF.amigos = mutableListOf(juanM, lucasP, andresH)
        sofiaL.amigos = mutableListOf(andresS, valentinaN, juanA)

        val todosLosUsuarios = listOf(
            juanA,
            agustinH,
            juanM,
            andresH,
            giulianaM,
            andresS,
            camilaR,
            lucasP,
            valentinaN,
            matiasF,
            sofiaL
        )

        usuarioRepository.saveAll(todosLosUsuarios)

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

        val grupoViaje = Grupo(
            nombre = "Escapada Cordoba",
            descripcion = "Organizacion de combustible y peajes",
            imagenUrl = "https://media.istockphoto.com/id/1365317354/photo/group-of-friends-on-road-trip.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(juanA, andresS, valentinaN, sofiaL).forEach { it ->
                agregarIntegrante(it, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(camilaR, RolIntegrante.ADMIN)
        }

        val grupoCumple = Grupo(
            nombre = "Cumple de Matu",
            descripcion = "Decoracion, comida y bebidas para el cumple",
            imagenUrl = "https://cdn0.bodas.com.mx/article-vendor/2267/3_2/1280/jpg/portada-2_5_112267-166458303624924.jpeg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(juanM, matiasF, agustinH, giulianaM).forEach { it ->
                agregarIntegrante(it, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(lucasP, RolIntegrante.ADMIN)
        }

        val grupoGym = Grupo(
            nombre = "Team 7AM",
            descripcion = "Suplementos y clases grupales",
            imagenUrl = "https://www.mensjournal.com/.image/t_share/MTk2MTM2NjA3NzQ5MzQxMzk3/men-working-out-in-gym.jpg",
            fechaCreacion = LocalDate.now()
        ).apply {
            listOf(andresH, juanA, sofiaL, matiasF).forEach { it ->
                agregarIntegrante(it, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(valentinaN, RolIntegrante.ADMIN)
        }

        val grupoHackLab = Grupo(
            nombre = "HackLab Nocturno",
            descripcion = "Juntadas para codear proyectos y aprender",
            imagenUrl = "https://images.unsplash.com/photo-1515879218367-8466d910aaa4",
            fechaCreacion = LocalDate.now()
        ).apply {
            todosLosUsuarios.filter { it != juanA }.forEach { usuario ->
                agregarIntegrante(usuario, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(juanA, RolIntegrante.ADMIN)
        }

        val grupoSeries = Grupo(
            nombre = "Maraton de Series",
            descripcion = "Suscripciones y picada para maratones del finde",
            imagenUrl = "https://images.unsplash.com/photo-1593784991095-a205069470b6",
            fechaCreacion = LocalDate.now()
        ).apply {
            todosLosUsuarios.filter { it != camilaR }.forEach { usuario ->
                agregarIntegrante(usuario, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(camilaR, RolIntegrante.ADMIN)
        }

        val grupoFinde = Grupo(
            nombre = "Plan de Finde",
            descripcion = "Salidas, transporte y gastos compartidos",
            imagenUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac",
            fechaCreacion = LocalDate.now()
        ).apply {
            todosLosUsuarios.filter { it != valentinaN }.forEach { usuario ->
                agregarIntegrante(usuario, RolIntegrante.MIEMBRO)
            }
            agregarIntegrante(valentinaN, RolIntegrante.ADMIN)
        }

        grupoRepository.saveAll(
            listOf(
                grupoAsado,
                grupoDepto,
                grupoFutbol,
                grupoEstudio,
                grupoViaje,
                grupoCumple,
                grupoGym,
                grupoHackLab,
                grupoSeries,
                grupoFinde
            )
        )

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
            ).apply { agregarItem("Paquete vasos", 1.0, 3500.0) },
            PartesIguales(
                "Compra supermercado",
                CategoriaGasto.COMIDA,
                32600.0,
                LocalDate.now(),
                grupoDepto,
                andresS,
                grupoDepto.usuarios()
            ).apply { agregarItem("Limpieza", 1.0, 10600.0); agregarItem("Comida semanal", 1.0, 22000.0) },
            PartesIguales(
                "Internet y cable",
                CategoriaGasto.HOGAR,
                18000.0,
                LocalDate.now(),
                grupoDepto,
                giulianaM,
                grupoDepto.usuarios()
            ).apply { agregarItem("Abono mensual", 1.0, 18000.0) },
            PartesIguales(
                "Gas envasado",
                CategoriaGasto.HOGAR,
                9600.0,
                LocalDate.now(),
                grupoDepto,
                juanM,
                grupoDepto.usuarios()
            ).apply { agregarItem("Recarga garrafa", 2.0, 4800.0) },
            PartesIguales(
                "Reserva de cancha",
                CategoriaGasto.VARIOS,
                24000.0,
                LocalDate.now(),
                grupoFutbol,
                giulianaM,
                grupoFutbol.usuarios()
            ).apply { agregarItem("Alquiler cancha", 1.0, 24000.0) },
            PartesIguales(
                "Pelota nueva",
                CategoriaGasto.VARIOS,
                18500.0,
                LocalDate.now(),
                grupoFutbol,
                andresH,
                grupoFutbol.usuarios()
            ).apply { agregarItem("Pelota N5", 1.0, 18500.0) },
            PartesIguales(
                "Tercer tiempo",
                CategoriaGasto.BEBIDA,
                14800.0,
                LocalDate.now(),
                grupoFutbol,
                juanM,
                grupoFutbol.usuarios()
            ).apply { agregarItem("Agua", 6.0, 800.0); agregarItem("Isotonica", 8.0, 1250.0) },
            PartesIguales(
                "Fotocopias final",
                CategoriaGasto.OTROS,
                7200.0,
                LocalDate.now(),
                grupoEstudio,
                giulianaM,
                grupoEstudio.usuarios()
            ).apply { agregarItem("Apuntes", 3.0, 2400.0) },
            PartesIguales(
                "Cafe de estudio",
                CategoriaGasto.COMIDA,
                5600.0,
                LocalDate.now(),
                grupoEstudio,
                juanA,
                grupoEstudio.usuarios()
            ).apply { agregarItem("Cafe doble", 4.0, 1400.0) },
            PartesIguales(
                "Combustible",
                CategoriaGasto.TRANSPORTE,
                68000.0,
                LocalDate.now(),
                grupoViaje,
                camilaR,
                grupoViaje.usuarios()
            ).apply { agregarItem("Nafta super", 40.0, 1700.0) },
            PartesIguales(
                "Peajes",
                CategoriaGasto.TRANSPORTE,
                21500.0,
                LocalDate.now(),
                grupoViaje,
                andresS,
                grupoViaje.usuarios()
            ).apply { agregarItem("Cabinas", 5.0, 4300.0) },
            PartesIguales(
                "Snacks ruta",
                CategoriaGasto.COMIDA,
                12400.0,
                LocalDate.now(),
                grupoViaje,
                sofiaL,
                grupoViaje.usuarios()
            ).apply { agregarItem("Galletitas", 4.0, 1100.0); agregarItem("Agua", 8.0, 1000.0) },
            PartesIguales(
                "Decoracion",
                CategoriaGasto.VARIOS,
                29500.0,
                LocalDate.now(),
                grupoCumple,
                lucasP,
                grupoCumple.usuarios()
            ).apply { agregarItem("Globos", 5.0, 1900.0); agregarItem("Banderines", 3.0, 6700.0) },
            PartesIguales(
                "Torta y velas",
                CategoriaGasto.COMIDA,
                36800.0,
                LocalDate.now(),
                grupoCumple,
                agustinH,
                grupoCumple.usuarios()
            ).apply { agregarItem("Torta", 1.0, 34000.0); agregarItem("Velas", 2.0, 1400.0) },
            PartesIguales(
                "Bebidas",
                CategoriaGasto.BEBIDA,
                22600.0,
                LocalDate.now(),
                grupoCumple,
                giulianaM,
                grupoCumple.usuarios()
            ).apply { agregarItem("Gaseosa", 8.0, 1600.0); agregarItem("Jugo", 6.0, 1600.0) },
            PartesIguales(
                "Cuota mensual",
                CategoriaGasto.OTROS,
                90000.0,
                LocalDate.now(),
                grupoGym,
                valentinaN,
                grupoGym.usuarios()
            ).apply { agregarItem("Pase grupal", 5.0, 18000.0) },
            PartesIguales(
                "Proteinas",
                CategoriaGasto.OTROS,
                45500.0,
                LocalDate.now(),
                grupoGym,
                matiasF,
                grupoGym.usuarios()
            ).apply { agregarItem("Whey", 2.0, 21000.0); agregarItem("Creatina", 1.0, 3500.0) },
            PartesIguales(
                "Clase funcional",
                CategoriaGasto.OTROS,
                28000.0,
                LocalDate.now(),
                grupoGym,
                sofiaL,
                grupoGym.usuarios()
            ).apply { agregarItem("Pack 10 clases", 1.0, 28000.0) },
            PartesIguales(
                "Suscripcion herramientas",
                CategoriaGasto.OTROS,
                36000.0,
                LocalDate.now(),
                grupoHackLab,
                juanA,
                grupoHackLab.usuarios()
            ).apply { agregarItem("GitHub Team", 1.0, 22000.0); agregarItem("Figma", 1.0, 14000.0) },
            PartesIguales(
                "Pizzas de deploy",
                CategoriaGasto.COMIDA,
                27500.0,
                LocalDate.now(),
                grupoHackLab,
                camilaR,
                grupoHackLab.usuarios()
            ).apply { agregarItem("Muzzarella", 3.0, 6500.0); agregarItem("Fugazzeta", 1.0, 8000.0) },
            PartesIguales(
                "Cafes y energia",
                CategoriaGasto.BEBIDA,
                12800.0,
                LocalDate.now(),
                grupoHackLab,
                andresH,
                grupoHackLab.usuarios()
            ).apply { agregarItem("Cafe", 8.0, 1000.0); agregarItem("Energizante", 3.0, 1600.0) },
            PartesIguales(
                "Suscripciones streaming",
                CategoriaGasto.ENTRETENIMIENTO,
                24900.0,
                LocalDate.now(),
                grupoSeries,
                camilaR,
                grupoSeries.usuarios()
            ).apply { agregarItem("Pack plataformas", 1.0, 24900.0) },
            PartesIguales(
                "Picada maraton",
                CategoriaGasto.COMIDA,
                19800.0,
                LocalDate.now(),
                grupoSeries,
                giulianaM,
                grupoSeries.usuarios()
            ).apply { agregarItem("Snacks", 6.0, 1800.0); agregarItem("Sandwiches", 3.0, 3000.0) },
            PartesIguales(
                "Gaseosas y jugos",
                CategoriaGasto.BEBIDA,
                14400.0,
                LocalDate.now(),
                grupoSeries,
                valentinaN,
                grupoSeries.usuarios()
            ).apply { agregarItem("Gaseosa 2.25", 6.0, 1700.0); agregarItem("Jugo", 3.0, 1400.0) },
            PartesIguales(
                "Nafta salida del sabado",
                CategoriaGasto.TRANSPORTE,
                45200.0,
                LocalDate.now(),
                grupoFinde,
                valentinaN,
                grupoFinde.usuarios()
            ).apply { agregarItem("Carga YPF", 1.0, 45200.0) },
            PartesIguales(
                "Asado del domingo",
                CategoriaGasto.COMIDA,
                53800.0,
                LocalDate.now(),
                grupoFinde,
                juanM,
                grupoFinde.usuarios()
            ).apply { agregarItem("Carne", 1.0, 37000.0); agregarItem("Carbon", 2.0, 4200.0); agregarItem("Ensaladas", 1.0, 8400.0) },
            PartesIguales(
                "Entradas recital",
                CategoriaGasto.ENTRETENIMIENTO,
                66000.0,
                LocalDate.now(),
                grupoFinde,
                lucasP,
                grupoFinde.usuarios()
            ).apply { agregarItem("Tickets campo", 4.0, 16500.0) }
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            // Lógica de filtrado de deudas propias
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)
        }
    }

}
