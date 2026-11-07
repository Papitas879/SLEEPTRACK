import os

base_path = "C:/xampp/htdocs/SLEEPTRACKAPP/app/src/main"
java_dir = os.path.join(base_path, "java/com/example/sleeptrackapp")
res_dir = os.path.join(base_path, "res/layout")

renames = [
    ("LoginActivity", "Hu03IniciarSesionActivity", "activity_login", "activity_hu03_iniciar_sesion"),
    ("Hu03ConsentimientoActivity", "Hu05ConsentimientoActivity", "activity_hu03_consentimiento", "activity_hu05_consentimiento"),
    ("Hu04ConfigurarDescansoActivity", "Hu06ConfigurarDescansoActivity", "activity_hu04_configurar_descanso", "activity_hu06_configurar_descanso"),
    ("Hu05MonitoreoActivity", "Hu07MonitoreoActivity", "activity_hu05_monitoreo", "activity_hu07_monitoreo"),
    ("Hu10ResumenDescansoActivity", "Hu12ResumenDescansoActivity", "activity_hu10_resumen_descanso", "activity_hu12_resumen_descanso"),
    ("Hu18AlertaLogroActivity", "Hu20AlertaLogroActivity", "activity_hu18_alerta_logro", "activity_hu20_alerta_logro"),
    ("Hu20PrivacidadDatosActivity", "Hu23PrivacidadDatosActivity", "activity_hu20_privacidad_datos", "activity_hu23_privacidad_datos")
]

# Rename files
for old_java, new_java, old_xml, new_xml in renames:
    old_j_path = os.path.join(java_dir, old_java + ".java")
    new_j_path = os.path.join(java_dir, new_java + ".java")
    old_x_path = os.path.join(res_dir, old_xml + ".xml")
    new_x_path = os.path.join(res_dir, new_xml + ".xml")

    if os.path.exists(old_j_path):
        os.rename(old_j_path, new_j_path)
    if os.path.exists(old_x_path):
        os.rename(old_x_path, new_x_path)

# Update contents
def replace_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as file:
        content = file.read()

    original_content = content
    for old_java, new_java, old_xml, new_xml in renames:
        content = content.replace(old_java, new_java)
        content = content.replace(old_xml, new_xml)

    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as file:
            file.write(content)

for root, _, files in os.walk(base_path):
    for f in files:
        if f.endswith(".java") or f.endswith(".xml"):
            replace_in_file(os.path.join(root, f))

print("Refactor complete")
