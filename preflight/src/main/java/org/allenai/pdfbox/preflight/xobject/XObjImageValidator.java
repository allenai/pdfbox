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

package org.allenai.pdfbox.preflight.xobject;

import java.io.IOException;

import org.allenai.pdfbox.cos.COSBase;
import org.allenai.pdfbox.cos.COSBoolean;
import org.allenai.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.allenai.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.ValidationResult;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.preflight.graphic.ColorSpaceHelperFactory;
import org.allenai.pdfbox.preflight.utils.COSUtils;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.preflight.PreflightConfiguration;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.graphic.ColorSpaceHelper;
import org.allenai.pdfbox.preflight.utils.RenderingIntents;

/**
 * This class validates XObject with the Image subtype.
 */
public class XObjImageValidator extends AbstractXObjValidator
{
    protected PDImageXObject xImage = null;

    public XObjImageValidator(PreflightContext context, PDImageXObject xobj)
    {
        super(context, xobj.getCOSStream());
        this.xImage = xobj;
    }

    @Override
    protected void checkMandatoryFields()
    {
        boolean res = this.xobject.getItem(COSName.WIDTH) != null;
        res = res && this.xobject.getItem(COSName.HEIGHT) != null;
        // type and subtype checked before to create the Validator.
        if (!res)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_FIELD));
        }
    }

    /*
     * 6.2.4 no Alternates
     */
    protected void checkAlternates() throws ValidationException
    {
        if (this.xobject.getItem("Alternates") != null)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY, "Unexpected 'Alternates' Key"));
        }
    }

    /*
     * 6.2.4 if interpolates, value = false
     */
    protected void checkInterpolate() throws ValidationException
    {
        if (this.xobject.getItem("Interpolate") != null && this.xobject.getBoolean("Interpolate", true))
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                    "Unexpected 'true' value for 'Interpolate' Key"));
        }
    }

    /*
     * 6.2.4 Intent has specific values
     */
    protected void checkIntent() throws ValidationException
    {
        if (this.xobject.getItem("Intent") != null)
        {
            String s = this.xobject.getNameAsString("Intent");
            if (!RenderingIntents.contains(s))
            {
                context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                        "Unexpected value '" + s + "' for Intent key in image"));
            }
        }
    }

    /*
     * According to the PDF Reference file, there are some specific rules on following fields ColorSpace, Mask,
     * ImageMask and BitsPerComponent. If ImageMask is set to true, ColorSpace and Mask entries are forbidden.
     */
    protected void checkColorSpaceAndImageMask() throws ValidationException
    {
        COSBase csImg = this.xobject.getItem(COSName.COLORSPACE);
        COSBase bitsPerComp = this.xobject.getItem("BitsPerComponent");
        COSBase mask = this.xobject.getItem(COSName.MASK);

        if (isImageMaskTrue())
        {
            if (csImg != null || mask != null)
            {
                context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
                        "ImageMask entry is true, ColorSpace and Mask are forbidden."));
            }

            Integer bitsPerCompValue = COSUtils.getAsInteger(bitsPerComp, cosDocument);
            if (bitsPerCompValue != null && bitsPerCompValue != 1)
            {
                context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                        "ImageMask entry is true, BitsPerComponent must be absent or 1."));
            }

        }
        else
        {
            try
            {
                PreflightConfiguration config = context.getConfig();
                ColorSpaceHelperFactory csFact = config.getColorSpaceHelperFact();
                PDColorSpace pdCS = PDColorSpace.create(csImg);
                ColorSpaceHelper csh = csFact.getColorSpaceHelper(context, pdCS, ColorSpaceHelperFactory.ColorSpaceRestriction.NO_PATTERN);
                csh.validate();
            }
            catch (IOException e)
            {
                context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_INVALID_UNKNOWN_COLOR_SPACE));
            }
        }
    }

    private boolean isImageMaskTrue()
    {
        COSBase imgMask = this.xobject.getItem("ImageMask");
        if (imgMask instanceof COSBoolean)
        {
            return ((COSBoolean) imgMask).getValue();
        }
        else
        {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator#validate()
     */
    @Override
    public void validate() throws ValidationException
    {
        super.validate();

        checkAlternates();
        checkInterpolate();
        checkIntent();

        checkColorSpaceAndImageMask();
    }
}
