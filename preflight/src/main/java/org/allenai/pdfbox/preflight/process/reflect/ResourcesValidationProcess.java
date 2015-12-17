/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.allenai.pdfbox.preflight.process.reflect;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.allenai.pdfbox.cos.COSBase;
import org.allenai.pdfbox.cos.COSStream;
import org.allenai.pdfbox.pdmodel.font.PDFont;
import org.allenai.pdfbox.pdmodel.font.PDFontFactory;
import org.allenai.pdfbox.pdmodel.graphics.PDXObject;
import org.allenai.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.allenai.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.allenai.pdfbox.pdmodel.graphics.shading.PDShading;
import org.allenai.pdfbox.preflight.PreflightConfiguration;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightPath;
import org.allenai.pdfbox.preflight.ValidationResult;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.preflight.process.AbstractProcess;
import org.allenai.pdfbox.preflight.utils.COSUtils;
import org.allenai.pdfbox.preflight.utils.ContextHelper;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSDocument;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.pdmodel.PDResources;
import org.allenai.pdfbox.preflight.PreflightContext;

public class ResourcesValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PreflightPath vPath = ctx.getValidationPath();
        if (vPath.isEmpty())
        {
        }
        else if (!vPath.isExpectedType(PDResources.class))
        {
            addValidationError(ctx, new ValidationResult.ValidationError(PreflightConstants.ERROR_PDF_PROCESSING_MISSING, "Resources validation process needs at least one PDResources object"));
        } 
        else
        {
            PDResources resources = (PDResources) vPath.peek();
            
            validateFonts(ctx, resources);
            validateExtGStates(ctx, resources);
            validateShadingPattern(ctx, resources);
            validateTilingPattern(ctx, resources);
            validateXObjects(ctx, resources);
        }
    }

    /**
     * Check that fonts present in the Resources dictionary match with PDF/A-1 rules
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateFonts(PreflightContext context, PDResources resources) throws ValidationException
    {
        Map<String, PDFont> mapOfFonts = getFonts(resources.getCOSObject(), context);
        if (mapOfFonts != null)
        {
            for (Entry<String, PDFont> entry : mapOfFonts.entrySet())
            {
                ContextHelper.validateElement(context, entry.getValue(), PreflightConfiguration.FONT_PROCESS);
            }
        }
    }

    /**
     * This will get the map of fonts. This will never return null.
     *
     * @return The map of fonts.
     */
    private Map<String, PDFont> getFonts(COSDictionary resources, PreflightContext context)
    {
        Map<String, PDFont> fonts = new HashMap<String, PDFont>();
        COSDictionary fontsDictionary = (COSDictionary) resources.getDictionaryObject(COSName.FONT);
        if (fontsDictionary == null)
        {
            fontsDictionary = new COSDictionary();
            resources.setItem(COSName.FONT, fontsDictionary);
        }
        for (COSName fontName : fontsDictionary.keySet())
        {
            COSBase font = fontsDictionary.getDictionaryObject(fontName);
            // data-000174.pdf contains a font that is a COSArray, looks to be an error in the
            // PDF, we will just ignore entries that are not dictionaries.
            if (font instanceof COSDictionary)
            {
                PDFont newFont = null;
                try
                {
                    newFont = PDFontFactory.createFont((COSDictionary) font);
                }
                catch (IOException e)
                {
                    addFontError((COSDictionary) font, context, e);
                }
                if (newFont != null)
                {
                    fonts.put(fontName.getName(), newFont);
                }
            }
        }
        return fonts;
    }

    /**
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateExtGStates(PreflightContext context, PDResources resources) throws ValidationException
    {
        COSBase egsEntry = resources.getCOSObject().getItem(PreflightConstants.TRANPARENCY_DICTIONARY_KEY_EXTGSTATE);
        COSDocument cosDocument = context.getDocument().getDocument();
        COSDictionary extGState = COSUtils.getAsDictionary(egsEntry, cosDocument);
        if (egsEntry != null)
        {
            ContextHelper.validateElement(context, extGState, PreflightConfiguration.EXTGSTATE_PROCESS);
        }
    }

    /**
     * This method check the Shading entry of the resource dictionary if exists.
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateShadingPattern(PreflightContext context, PDResources resources) throws ValidationException
    {
        try
        {
            for (COSName name : resources.getShadingNames())
            {
                PDShading shading = resources.getShading(name);
                ContextHelper.validateElement(context, shading, PreflightConfiguration.SHADING_PATTERN_PROCESS);
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage(), e));
        }
    }

    /**
     * This method check the Shading entry of the resource dictionary if exists.
     * 
     * @param context
     * @param resources
     * @throws ValidationException
     */
    protected void validateTilingPattern(PreflightContext context, PDResources resources) throws ValidationException
    {
        try
        {
            for (COSName name : resources.getPatternNames())
            {
                PDAbstractPattern pattern = resources.getPattern(name);
                if (pattern instanceof PDTilingPattern)
                {
                    ContextHelper.validateElement(context, pattern, PreflightConfiguration.TILING_PATTERN_PROCESS);
                }
            }
        }
        catch (IOException e)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION, e.getMessage(), e));
        }
    }

    protected void validateXObjects(PreflightContext context, PDResources resources) throws ValidationException
    {
        COSDocument cosDocument = context.getDocument().getDocument();
        COSDictionary mapOfXObj = COSUtils.getAsDictionary(resources.getCOSObject().getItem(COSName.XOBJECT),
                cosDocument);
        if (mapOfXObj != null)
        {
            for (Entry<COSName, COSBase> entry : mapOfXObj.entrySet())
            {
                COSBase xobj = entry.getValue();
                if (xobj != null && COSUtils.isStream(xobj, cosDocument))
                {
                    try
                    {
                        COSStream stream = COSUtils.getAsStream(xobj, cosDocument);
                        PDXObject pdXObject = PDXObject.createXObject(stream, resources);
                        if (pdXObject != null)
                        {
                            ContextHelper.validateElement(context, pdXObject, PreflightConfiguration.GRAPHIC_PROCESS);
                        }
                        else
                        {
                            ContextHelper.validateElement(context, stream, PreflightConfiguration.GRAPHIC_PROCESS);
                        }
                    }
                    catch (IOException e)
                    {
                        context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_MAIN,
                                e.getMessage() + " for entry '"
                                + entry.getKey().getName() + "'", e));
                    }
                }
            }
        }
    }
}
