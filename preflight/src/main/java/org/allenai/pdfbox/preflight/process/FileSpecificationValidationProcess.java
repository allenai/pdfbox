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

package org.allenai.pdfbox.preflight.process;

import java.util.ArrayList;
import java.util.List;

import org.allenai.pdfbox.cos.COSBase;
import org.allenai.pdfbox.cos.COSObject;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSDocument;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.pdmodel.PDDocument;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * 
 * This validation process check that FileSpec dictionaries are confirming with the PDF/A-1b specification.
 */
public class FileSpecificationValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext ctx) throws ValidationException
    {
        PDDocument pdfDoc = ctx.getDocument();
        COSDocument cDoc = pdfDoc.getDocument();

        List<?> lCOSObj = cDoc.getObjects();
        for (Object o : lCOSObj)
        {
            COSBase cBase = ((COSObject) o).getObject();
            if (cBase instanceof COSDictionary)
            {
                COSDictionary dic = (COSDictionary) cBase;
                String type = dic.getNameAsString(COSName.TYPE);
                if (PreflightConstants.FILE_SPECIFICATION_VALUE_TYPE.equals(type) || COSName.F.getName().equals(type))
                {
                    // ---- It is a file specification
                    validateFileSpecification(ctx, dic);
                }
            }
        }
    }

    /**
     * Validate a FileSpec dictionary, a FileSpec dictionary mustn't have the EF (EmbeddedFile) entry.
     * 
     * @param ctx the preflight context.
     * @param fileSpec the FileSpec Dictionary.
     * @return the list of validation errors.
     */
    public List<ValidationError> validateFileSpecification(PreflightContext ctx, COSDictionary fileSpec)
    {
        List<ValidationError> result = new ArrayList<ValidationError>(0);

        // ---- Check dictionary entries
        // ---- Only the EF entry is forbidden
        if (fileSpec.getItem(COSName.getPDFName(PreflightConstants.FILE_SPECIFICATION_KEY_EMBEDDED_FILE)) != null)
        {
            addValidationError(ctx, new ValidationError(PreflightConstants.ERROR_SYNTAX_EMBEDDED_FILES,
                    "EmbeddedFile entry is present in a FileSpecification dictionary"));
        }

        return result;
    }
}
