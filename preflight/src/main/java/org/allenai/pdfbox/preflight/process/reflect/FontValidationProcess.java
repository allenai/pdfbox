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

import org.allenai.pdfbox.pdmodel.font.PDFont;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightPath;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.preflight.font.TrueTypeFontValidator;
import org.allenai.pdfbox.preflight.font.Type3FontValidator;
import org.allenai.pdfbox.pdmodel.font.PDSimpleFont;
import org.allenai.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.allenai.pdfbox.pdmodel.font.PDType3Font;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.font.FontValidator;
import org.allenai.pdfbox.preflight.font.Type0FontValidator;
import org.allenai.pdfbox.preflight.font.Type1FontValidator;
import org.allenai.pdfbox.preflight.font.container.FontContainer;
import org.allenai.pdfbox.preflight.process.AbstractProcess;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;


public class FontValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) 
        {
            return;
        }
        if (!vPath.isExpectedType(PDFont.class))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_FONTS_INVALID_DATA,
                    "Font validation process needs at least one PDFont object"));
        } 
        else
        {
            PDFont font = (PDFont) vPath.peek();
            FontContainer fontContainer = context.getFontContainer(font.getCOSObject());
            if (fontContainer == null)
            {
                // if fontContainer isn't null the font is already checked
                FontValidator<? extends FontContainer> validator = getFontValidator(context, font);
                if (validator != null)
                {
                    validator.validate();
                }
            }
        }
    }

    /**
     * Create the right "Validator" object for the given font type
     * 
     * @param context the preflight context.
     * @param font the font object.
     * @return the font validator instance for the font type.
     */
    protected FontValidator<? extends FontContainer> getFontValidator(PreflightContext context, PDFont font)
    {
        String subtype = font.getSubType();
        if (PreflightConstants.FONT_DICTIONARY_VALUE_TRUETYPE.equals(subtype))
        {
            return new TrueTypeFontValidator(context, (PDTrueTypeFont) font);
        }
        if (PreflightConstants.FONT_DICTIONARY_VALUE_MMTYPE.equals(subtype) || PreflightConstants.FONT_DICTIONARY_VALUE_TYPE1.equals(subtype))
        {
            return new Type1FontValidator(context, (PDSimpleFont) font);
        }
        if (PreflightConstants.FONT_DICTIONARY_VALUE_TYPE3.equals(subtype))
        {
            return new Type3FontValidator(context, (PDType3Font) font);
        }
        if (PreflightConstants.FONT_DICTIONARY_VALUE_COMPOSITE.equals(subtype))
        {
            return new Type0FontValidator(context, font);
        }
        if (PreflightConstants.FONT_DICTIONARY_VALUE_TYPE2.equals(subtype) || PreflightConstants.FONT_DICTIONARY_VALUE_TYPE1C.equals(subtype)
                || PreflightConstants.FONT_DICTIONARY_VALUE_TYPE0C.equals(subtype) || PreflightConstants.FONT_DICTIONARY_VALUE_TYPE0.equals(subtype))
        {
            // ---- Font managed by a Composite font.
            // this dictionary will be checked by a CompositeFontValidator
            return null;
        }
        context.addValidationError(new ValidationError(PreflightConstants.ERROR_FONTS_UNKNOWN_FONT_TYPE, "Unknown font type: " + subtype));
        return null;
    }

}
