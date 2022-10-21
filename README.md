# MLKit-Pose-Detection-CameraX-Basic
Combining the Google ML Kit with live camera using CameraX API

#COMMIT 1:

Uso básico de CameraX con previsualización y Análisis de Imagen
Basado en el tutorial: https://developer.android.com/codelabs/camerax-getting-started
También se piden los permisos al usuario.

#COMMIT 2:

Se incorpora MLKit-Pose-Detection.
Solo muestra un Log indicando si se ha encontrado una persona.
Basado en: https://developers.google.com/ml-kit/vision/pose-detection/android

#COMMIT 3:

Se incorpora GraphicOverlay.
Cuando se detecta una persona se visualiza “persona” en las coordenadas (100,100) de pantalla.
La clase GraphicOverlay está sacada de https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart.
OjO: Se ha comentado el cálculo de Z

#COMMIT 4:

Se añade PoseGraphic que visualiza las pose detecctada.
Hay que llamar a graphicOverlay.setImageSourceInfo() para calcular el ajuste de coordenadas. 
Esta puesto en PoseAnalyzer.analyze(). Pero posiblemente no sea el mejor sitio.

#COMMIT 5:

Mejoras varias:
-GraphicOverlay: La clase Preconditions se implementa con Log.e
-GraphicOverlay: En el commit anterior se comentó la llamada a Int.constrainToRange al no encotrarse el paquete. Para solucionarlo se define esta función.
-Se saca la clase PoseAnalyzer de MainActivity
-PoseGraphic: Se traduce las partes del cuerpo (Knee -> Rodilla)
-Se cambian varias declaraciones de Kotlin para seguir las recomendaciones

#COMMIT 6:

Líneas con uno de sus puntos con poca probabilidad, no son dibujadas. 
Los puntos y su probabilidad si son dibujados.
PoseGraphic: se añade un nuevo parámetro drawUnlikelyLines
 
