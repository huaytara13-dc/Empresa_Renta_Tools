<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>RentaTools - Cuenta bloqueada</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: Arial, Helvetica, sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #0b2559;
            padding: 20px;
        }
        .tarjeta {
            background: #fff;
            max-width: 440px;
            width: 100%;
            padding: 40px 32px;
            border-radius: 14px;
            text-align: center;
            border-top: 6px solid #fa5a14;
        }
        h1 { color: #0b2559; font-size: 26px; margin-bottom: 14px; }
        p  { color: #5f6672; line-height: 1.6; margin-bottom: 10px; }
        a  {
            display: inline-block;
            margin-top: 18px;
            padding: 12px 28px;
            background: #fa5a14;
            color: #fff;
            text-decoration: none;
            border-radius: 8px;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="tarjeta">
        <h1>Cuenta bloqueada</h1>
        <p>Superó el máximo de <strong>3 intentos</strong> fallidos.</p>
        <p>Espere unos minutos o contacte al administrador.</p>
        <p id="aviso">Esta ventana se cerrará en 5 segundos…</p>
        <a href="login.jsp">Volver al inicio</a>
    </div>

    <script>
        // window.close() solo funciona si la pestaña fue abierta por un script.
        // Si el navegador lo impide, queda visible el boton "Volver al inicio".
        setTimeout(function () {
            window.close();
            document.getElementById('aviso').textContent =
                'No se pudo cerrar la ventana automáticamente. Puede cerrarla manualmente.';
        }, 5000);
    </script>
</body>
</html>
