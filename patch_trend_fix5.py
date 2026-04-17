import re

def patch():
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    # Rewrite pointerInputs to not use fully qualified extensions
    old_pointer = """.androidx.compose.ui.input.pointer.pointerInput"""
    if old_pointer in content:
        content = content.replace(old_pointer, ".pointerInput")

    # Change to explicitly pass lambda args as they might be failing inference
    old_detect_drag = """detectDragGestures(
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null }
                            ) { change, _ ->"""
    new_detect_drag = """detectDragGestures(
                                onDragEnd = { hoveredIndex = null },
                                onDragCancel = { hoveredIndex = null },
                                onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, _: androidx.compose.ui.geometry.Offset ->"""
    content = content.replace(old_detect_drag, new_detect_drag)

    old_detect_tap = """detectTapGestures(
                                onTap = { offset ->"""
    new_detect_tap = """detectTapGestures(
                                onTap = { offset: androidx.compose.ui.geometry.Offset ->"""
    content = content.replace(old_detect_tap, new_detect_tap)
    
    # Ensure import is explicitly there
    if "import androidx.compose.ui.input.pointer.pointerInput" not in content:
        content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.input.pointer.pointerInput")

    # Make absolute sure PointerInputChange is imported just in case though we qualified it above
    
    with open('app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt', 'w', encoding='utf-8') as f:
        f.write(content)

patch()
