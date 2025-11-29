import sys
import torch
from PIL import Image
from transformers import AutoImageProcessor, AutoModelForImageClassification

# Modelo optimizado para detectar fotos reales vs Midjourney/SD
MODEL_NAME = "jacoballessio/ai-image-detect-distilled"

def detect_photo(image_path):
    try:
        # Cargar imagen
        image = Image.open(image_path).convert("RGB")
        
        # Cargar procesador y modelo
        processor = AutoImageProcessor.from_pretrained(MODEL_NAME)
        model = AutoModelForImageClassification.from_pretrained(MODEL_NAME)
        
        # Preprocesar
        inputs = processor(images=image, return_tensors="pt")
        
        # Inferencia (sin gradientes para ahorrar memoria)
        with torch.no_grad():
            outputs = model(**inputs)
            logits = outputs.logits
            
        # Obtener predicción
        predicted_class_idx = logits.argmax(-1).item()
        label = model.config.id2label[predicted_class_idx]
        
        # La etiqueta suele ser "fake" (IA) o "real" (Humano)
        # Adaptamos la salida a TRUE (es IA) o FALSE (es Humano)
        if label.lower() in ["fake", "ai", "artificial"]:
            print("TRUE")
        else:
            print("FALSE")
            
    except Exception as e:
        # En caso de error, imprimimos algo que tu log de Java pueda capturar
        sys.stderr.write(str(e))
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python detect_photo.py <image_path>")
        sys.exit(1)
    
    detect_photo(sys.argv[1])