package com.example.insectclassifier;

import android.graphics.Bitmap;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class Classifier {

    Module model;
    public Classifier(String modelName){
        model = Module.load(modelName);
    }

    public int findBestClassIndex(float[] logits){
        float max_score = -Float.MAX_VALUE;
        int index = -1;
        for(int i = 0; i < logits.length; i++){
            if(logits[i] > max_score) {
                max_score = logits[i];
                index = i;
            }
        }
        return index;
    }

    public String makePrediction(Bitmap image){
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};

        image = Bitmap.createScaledBitmap(image, 244, 244, false);
        Tensor tensor = TensorImageUtils.bitmapToFloat32Tensor(image, mean, std);
        final Tensor result = model.forward(IValue.from(tensor)).toTensor();
        float[] scores = result.getDataAsFloatArray();

        int index = findBestClassIndex(scores);
        return Classes.ALL_INSECTS[index];
    }
}
