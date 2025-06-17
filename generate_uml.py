import os
import re
from typing import List, Dict, Set
import graphviz
import sys

# Add Graphviz to PATH
GRAPHVIZ_PATHS = [
    r"C:\Program Files\Graphviz\bin",
    r"C:\Program Files (x86)\Graphviz\bin",
    r"C:\Program Files\Graphviz2.38\bin",
]

for path in GRAPHVIZ_PATHS:
    if os.path.exists(path):
        os.environ["PATH"] = path + os.pathsep + os.environ["PATH"]
        break
else:
    print("Warning: Graphviz installation not found in common locations.")
    print("Please enter the path to your Graphviz installation (the folder containing dot.exe):")
    custom_path = input().strip()
    if os.path.exists(custom_path):
        os.environ["PATH"] = custom_path + os.pathsep + os.environ["PATH"]
    else:
        print("Error: Invalid path provided. Please make sure Graphviz is properly installed.")
        sys.exit(1)

class JavaClass:
    def __init__(self, name: str, package: str):
        self.name = name
        self.package = package
        self.extends = ""
        self.implements: List[str] = []
        self.fields: List[str] = []
        self.methods: List[str] = []
        self.is_interface = False

def parse_java_file(file_path: str) -> JavaClass:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Extract package name
    package_match = re.search(r'package\s+([\w.]+);', content)
    package = package_match.group(1) if package_match else ""

    # Get class name from filename
    class_name = os.path.basename(file_path)[:-5]  # Remove .java
    java_class = JavaClass(class_name, package)

    # Check if it's an interface
    if re.search(r'interface\s+' + class_name, content):
        java_class.is_interface = True

    # Extract extends
    extends_match = re.search(rf'(?:class|interface)\s+{class_name}\s+extends\s+([\w.]+)', content)
    if extends_match:
        java_class.extends = extends_match.group(1)

    # Extract implements
    implements_match = re.search(rf'class\s+{class_name}.*?implements\s+([\w.,\s]+){{', content)
    if implements_match:
        implements = implements_match.group(1).strip()
        java_class.implements = [i.strip() for i in implements.split(',')]

    # Extract fields
    field_pattern = r'(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?([\w<>[\],\s]+)\s+(\w+)\s*[;=]'
    fields = re.finditer(field_pattern, content)
    for field in fields:
        type_name = field.group(1).strip()
        field_name = field.group(2)
        java_class.fields.append(f"{field_name}: {type_name}")

    # Extract methods
    method_pattern = r'(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?([\w<>[\],\s]+)\s+(\w+)\s*\([^)]*\)'
    methods = re.finditer(method_pattern, content)
    for method in methods:
        return_type = method.group(1).strip()
        method_name = method.group(2)
        if method_name != class_name:  # Skip constructors
            java_class.methods.append(f"{method_name}(): {return_type}")

    return java_class

def generate_graphviz(classes: List[JavaClass]) -> graphviz.Digraph:
    # Create a new directed graph
    dot = graphviz.Digraph(comment='Android Project UML Diagram')
    dot.attr(rankdir='BT')  # Bottom to top direction
    
    # Set default node attributes for better UML style
    dot.attr('node', shape='record', style='filled', fillcolor='lightgrey')
    
    # First pass: Create all nodes (classes and interfaces)
    for java_class in classes:
        # Prepare label with class name, fields, and methods
        label_parts = [f"{java_class.name}"]
        
        if java_class.fields:
            fields_str = "|" + "\\l".join(java_class.fields) + "\\l"
            label_parts.append(fields_str)
            
        if java_class.methods:
            methods_str = "|" + "\\l".join(java_class.methods) + "\\l"
            label_parts.append(methods_str)
        
        node_label = "{" + "".join(label_parts) + "}"
        
        # Different style for interfaces
        if java_class.is_interface:
            dot.node(java_class.name, node_label, style='filled', fillcolor='lightblue')
        else:
            dot.node(java_class.name, node_label)
    
    # Second pass: Add relationships
    for java_class in classes:
        # Add inheritance relationships
        if java_class.extends:
            dot.edge(java_class.name, java_class.extends, arrowhead='empty')
        
        # Add interface implementation relationships
        for interface in java_class.implements:
            dot.edge(java_class.name, interface, arrowhead='empty', style='dashed')
    
    return dot

def main():
    java_files = []
    root_dir = "app/src/main/java/com/example/sa3id"
    
    # Walk through all directories
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))

    # Parse all Java files
    classes = []
    for java_file in java_files:
        try:
            java_class = parse_java_file(java_file)
            classes.append(java_class)
        except Exception as e:
            print(f"Error parsing {java_file}: {str(e)}")

    # Generate Graphviz diagram
    dot = generate_graphviz(classes)
    
    # Save the diagram in multiple formats
    dot.render('uml_diagram', format='png', cleanup=True)  # PNG format
    dot.render('uml_diagram', format='pdf', cleanup=True)  # PDF format
    dot.render('uml_diagram', format='svg', cleanup=True)  # SVG format
    
    print("UML diagram has been generated in multiple formats:")
    print("- uml_diagram.png (for quick viewing)")
    print("- uml_diagram.pdf (for high-quality print)")
    print("- uml_diagram.svg (for web viewing)")

if __name__ == "__main__":
    main() 