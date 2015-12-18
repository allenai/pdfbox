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

import static org.allenai.pdfbox.preflight.PreflightConfiguration.RESOURCES_PROCESS;

import org.allenai.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightPath;
import org.allenai.pdfbox.preflight.ValidationResult;
import org.allenai.pdfbox.preflight.content.PreflightContentStream;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.preflight.utils.ContextHelper;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.pdmodel.PDPage;
import org.allenai.pdfbox.pdmodel.PDResources;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.process.AbstractProcess;

public class TilingPatternValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) 
        {
            return;
        }
        else if (!vPath.isExpectedType(PDTilingPattern.class))
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_OBJECT, "Tiling pattern validation required at least a PDPage"));
        }
        else
        {
            PDTilingPattern tilingPattern = (PDTilingPattern) vPath.peek();
            PDPage page = vPath.getClosestPathElement(PDPage.class);

            checkMandatoryFields(context, page, tilingPattern);
            parseResources(context, page, tilingPattern);
            parsePatternContent(context, page, tilingPattern);
        }
    }

    protected void parseResources(PreflightContext context, PDPage page, PDTilingPattern pattern)
            throws ValidationException
    {
        PDResources resources = pattern.getResources();
        if (resources != null)
        {
            ContextHelper.validateElement(context, resources, RESOURCES_PROCESS);
        }
    }

    /**
     * Validate the Pattern content like Color and Show Text Operators using an instance of ContentStreamWrapper.
     */
    protected void parsePatternContent(PreflightContext context, PDPage page, PDTilingPattern pattern)
            throws ValidationException
    {
        PreflightContentStream csWrapper = new PreflightContentStream(context, page);
        csWrapper.validatePatternContentStream(pattern);
    }

    /**
     * This method checks if required fields are present.
     */
    protected void checkMandatoryFields(PreflightContext context, PDPage page, PDTilingPattern pattern)
    {
        COSDictionary dictionary = pattern.getCOSObject();
        boolean res = dictionary.getItem(COSName.RESOURCES) != null;
        res = res && dictionary.getItem(COSName.BBOX) != null;
        res = res && dictionary.getItem(COSName.PAINT_TYPE) != null;
        res = res && dictionary.getItem(COSName.TILING_TYPE) != null;
        res = res && dictionary.getItem(COSName.X_STEP) != null;
        res = res && dictionary.getItem(COSName.Y_STEP) != null;
        if (!res)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_GRAPHIC_INVALID_PATTERN_DEFINITION));
        }
    }
}
