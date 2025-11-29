import sys
import torch
from PIL import Image
from transformers import ViTImageProcessor, ViTForImageClassification

# Modelo especializado en Anime/Ilustración (precisión ~94%)
MODEL_NAME = "legekka/AI-Anime-Image-Detector-ViT"

def detect_illustration(image_path):
    try:
        image = Image.open(image_path).convert("RGB")
        
        processor = ViTImageProcessor.from_pretrained(MODEL_NAME)
        model = ViTForImageClassification.from_pretrained(MODEL_NAME)
        
        inputs = processor(images=image, return_tensors="pt")
        
        with torch.no_grad():
            outputs = model(**inputs)
            logits = outputs.logits
        
        # Este modelo específico usa etiquetas como "AI" y "Human" (o índices 0/1)
        # Verificamos los logits directamente o las etiquetas del config
        probs = torch.nn.functional.softmax(logits, dim=-1)
        
        # El modelo de Legekka suele tener id2label = {0: 'artificial', 1: 'human'} o similar.
        # Lo verificamos dinámicamente:
        predicted_class_idx = logits.argmax(-1).item()
        label = model.config.id2label[predicted_class_idx]
        
        # Lógica de salida
        if "artificial" in label.lower() or "ai" in label.lower():
            print("TRUE")
        else:
            print("FALSE")

    except Exception as e:
        sys.stderr.write(str(e))
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python detect_illustration.py <image_path>")
        sys.exit(1)
    
    detect_illustration(sys.argv[1])