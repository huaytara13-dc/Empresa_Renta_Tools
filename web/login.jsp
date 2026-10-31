<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RentaTools - Iniciar sesión</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:ital,wght@0,400;0,500;0,600;0,700;0,800;1,800&display=swap" rel="stylesheet">
    <style>
        :root {
            --azul: #0b2559;
            --naranja: #fa5a14;
            --naranja-oscuro: #e04a0a;
            --texto: #5f6672;
            --borde: #dcdfe6;
            --fondo: #fefefe;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: 'Inter', Arial, Helvetica, sans-serif;
            background: var(--fondo);
            min-height: 100vh;
        }

        .contenedor {
            display: flex;
            min-height: 100vh;
        }

        /* ---------- Lado izquierdo ---------- */
        .lado-form {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
            padding: 44px 56px;
        }

        .logo {
            font-size: 40px;
            font-weight: 800;
            font-style: italic;
            letter-spacing: -1.5px;
            color: var(--azul);
            line-height: 1;
        }
        .logo span { color: var(--naranja); }

        .form-wrap {
            width: 100%;
            max-width: 460px;
            margin: auto;
            padding: 24px 0;
        }

        h1 {
            font-size: clamp(34px, 3.2vw, 50px);
            font-weight: 800;
            letter-spacing: -1px;
            color: var(--azul);
            text-align: center;
        }
        h1::after {
            content: "";
            display: block;
            width: 64px;
            height: 4px;
            margin: 14px auto 0;
            border-radius: 2px;
            background: var(--naranja);
        }
        .sub {
            text-align: center;
            color: var(--texto);
            font-size: 17px;
            margin: 20px 0 40px;
        }

        .campo { margin-bottom: 22px; }
        .campo label {
            display: block;
            font-size: 15px;
            font-weight: 700;
            color: var(--azul);
            margin-bottom: 9px;
        }
        .input-wrap { position: relative; }
        .input-wrap input {
            width: 100%;
            height: 56px;
            padding: 0 52px;
            background: #f8f9fb;
            border: 1px solid var(--borde);
            border-radius: 10px;
            font-family: inherit;
            font-size: 16px;
            color: #222;
            transition: border-color .2s, box-shadow .2s, background .2s;
        }
        .input-wrap input::placeholder { color: #8a909b; }
        .input-wrap input:focus {
            outline: none;
            background: #fff;
            border-color: var(--naranja);
            box-shadow: 0 0 0 3px rgba(250, 90, 20, .15);
        }
        .icono {
            position: absolute;
            left: 17px;
            top: 50%;
            transform: translateY(-50%);
            width: 22px;
            height: 22px;
            stroke: var(--azul);
            fill: none;
            stroke-width: 1.8;
            stroke-linecap: round;
            stroke-linejoin: round;
            pointer-events: none;
        }
        .ver-clave {
            position: absolute;
            right: 8px;
            top: 50%;
            transform: translateY(-50%);
            width: 40px;
            height: 40px;
            border: none;
            background: transparent;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .ver-clave svg {
            width: 22px;
            height: 22px;
            stroke: var(--azul);
            fill: none;
            stroke-width: 1.8;
            stroke-linecap: round;
            stroke-linejoin: round;
        }
        .ver-clave .tachado { display: none; }
        .ver-clave.activo .tachado { display: inline; }

        .btn-ingresar {
            width: 100%;
            height: 58px;
            margin-top: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            border: none;
            border-radius: 10px;
            background: var(--naranja);
            color: #fff;
            font-family: inherit;
            font-size: 16px;
            font-weight: 700;
            letter-spacing: .6px;
            text-transform: uppercase;
            cursor: pointer;
            transition: background .2s, transform .15s, box-shadow .15s;
        }
        .btn-ingresar svg {
            width: 24px;
            height: 24px;
            stroke: #fff;
            fill: none;
            stroke-width: 2;
            stroke-linecap: round;
            stroke-linejoin: round;
        }
        .btn-ingresar:hover {
            background: var(--naranja-oscuro);
            transform: translateY(-1px);
            box-shadow: 0 8px 18px rgba(250, 90, 20, .35);
        }

        .error {
            background: #fdecea;
            color: #b3261e;
            border: 1px solid #f5c6c2;
            padding: 12px;
            border-radius: 10px;
            margin-bottom: 22px;
            font-size: 14px;
            text-align: center;
        }

        /* ---------- Lado derecho: imagen ---------- */
        .panel-img {
            flex: 0 0 auto;
            display: block;
            height: 100vh;
            width: auto;
            max-width: 60vw;
            object-fit: cover;
            object-position: left center;
        }

        /* ---------- Tablet y celular: solo el formulario ---------- */
        @media (max-width: 900px) {
            .panel-img { display: none; }
            .lado-form { padding: 32px 24px; }
            .logo { font-size: 34px; text-align: center; }
        }
    </style>
</head>
<body>
    <div class="contenedor">

        <div class="lado-form">
            <div class="logo">Renta<span>Tools</span></div>

            <div class="form-wrap">
                <h1>Iniciar sesión</h1>
                <p class="sub">Ingresa con tu usuario y contraseña</p>

                <% String error = (String) request.getAttribute("error");
                   if (error != null) { %>
                    <div class="error" role="alert"><%= error %></div>
                <% } %>

                <form action="login" method="post">
                    <div class="campo">
                        <label for="usuario">Usuario</label>
                        <div class="input-wrap">
                            <svg class="icono" viewBox="0 0 24 24" aria-hidden="true">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                            <input type="text" id="usuario" name="usuario" placeholder="Usuario"
                                   autocomplete="username" required autofocus>
                        </div>
                    </div>

                    <div class="campo">
                        <label for="password">Contraseña</label>
                        <div class="input-wrap">
                            <svg class="icono" viewBox="0 0 24 24" aria-hidden="true">
                                <rect x="4" y="11" width="16" height="10" rx="2"/>
                                <path d="M8 11V7a4 4 0 0 1 8 0v4"/>
                            </svg>
                            <input type="password" id="password" name="password" placeholder="Contraseña"
                                   autocomplete="current-password" required>
                            <button type="button" class="ver-clave" id="btnVer"
                                    aria-label="Mostrar u ocultar contraseña">
                                <svg viewBox="0 0 24 24" aria-hidden="true">
                                    <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/>
                                    <circle cx="12" cy="12" r="3"/>
                                    <line class="tachado" x1="3" y1="3" x2="21" y2="21"/>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="btn-ingresar">
                        Iniciar sesión
                        <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path d="M5 12h14M13 6l6 6-6 6"/>
                        </svg>
                    </button>
                </form>
            </div>
        </div>

        <img class="panel-img" src="${pageContext.request.contextPath}/img/herramientas.png" alt="Herramientas profesionales RentaTools">
    </div>

    <script>
        var btnVer = document.getElementById('btnVer');
        var clave = document.getElementById('password');
        btnVer.addEventListener('click', function () {
            var visible = clave.type === 'text';
            clave.type = visible ? 'password' : 'text';
            btnVer.classList.toggle('activo', !visible);
        });
    </script>
</body>
</html>
