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

package org.allenai.pdfbox.preflight.font.descriptor;

import org.allenai.pdfbox.cos.COSStream;
import org.allenai.pdfbox.pdmodel.font.PDFontDescriptor;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.pdmodel.common.PDStream;
import org.allenai.pdfbox.pdmodel.font.PDSimpleFont;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;
import org.allenai.pdfbox.preflight.font.container.Type1Container;

public class Type1DescriptorHelper extends FontDescriptorHelper<Type1Container>
{
    public Type1DescriptorHelper(PreflightContext context, PDSimpleFont font, Type1Container fontContainer)
    {
        super(context, font, fontContainer);
    }

    @Override
    protected boolean checkMandatoryFields(COSDictionary fDescriptor)
    {
        boolean result = super.checkMandatoryFields(fDescriptor);
        /*
         * if this font is a subset, the CharSet entry must be present in the FontDescriptor
         */
        if (isSubSet(fontDescriptor.getFontName()))
        {
            String charsetStr = fontDescriptor.getCharSet();
            if (charsetStr == null || "".equals(charsetStr))
            {
                this.fContainer.push(new ValidationError(PreflightConstants.ERROR_FONTS_CHARSET_MISSING_FOR_SUBSET,
                        fontDescriptor.getFontName()
                        + ": The Charset entry is missing for the Type1 Subset"));
                result = false;
            }
        }
        return result;
    }

    @Override
    public PDStream extractFontFile(PDFontDescriptor fontDescriptor)
    {
        PDStream ff1 = fontDescriptor.getFontFile();
        PDStream ff3 = fontDescriptor.getFontFile3();

        if (ff1 != null)
        {
            COSStream stream = ff1.getStream();
            if (stream == null)
            {
                this.fContainer.push(new ValidationError(PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID,
                        fontDescriptor.getFontName() + ": The FontFile is missing"));
                this.fContainer.notEmbedded();
                return null;
            }

            boolean hasLength1 = stream.getInt(COSName.LENGTH1) > 0;
            boolean hasLength2 = stream.getInt(COSName.getPDFName(PreflightConstants.FONT_DICTIONARY_KEY_LENGTH2)) > 0;
            boolean hasLength3 = stream.getInt(COSName.getPDFName(PreflightConstants.FONT_DICTIONARY_KEY_LENGTH3)) >= 0;
            if (!(hasLength1 && hasLength2 && hasLength3))
            {
                this.fContainer.push(new ValidationError(PreflightConstants.ERROR_FONTS_FONT_FILEX_INVALID,
                        fontDescriptor.getFontName() + ": The FontFile is invalid"));
                return null;
            }

            return ff1;
        }
        else
        {
            return ff3;
        }
    }

    @Override
    protected void processFontFile(PDFontDescriptor fontDescriptor, PDStream fontFile)
    {
        if (font.isDamaged())
        {
            this.fContainer.push(new ValidationError(PreflightConstants.ERROR_FONTS_TYPE1_DAMAGED,
                    this.font.getName() + ": The FontFile can't be read"));
        }
    }
}
