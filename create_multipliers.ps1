Add-Type -AssemblyName System.Drawing

$font = New-Object System.Drawing.Font("Arial", 8, [System.Drawing.FontStyle]::Bold)
$brush = [System.Drawing.Brushes]::White

for ($i = 2; $i -le 10; $i++) {
    $bitmap = New-Object System.Drawing.Bitmap 16, 16
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.DrawString("x$i", $font, $brush, 0, 0)
    $bitmap.Save("src/main/resources/assets/itemlist/textures/multipliers/x$i.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}
