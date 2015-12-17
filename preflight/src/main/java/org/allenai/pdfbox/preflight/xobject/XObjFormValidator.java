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

import static org.allenai.pdfbox.preflight.PreflightConfiguration.RESOURCES_PROCESS;

import org.allenai.pdfbox.cos.COSBase;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightPath;
import org.allenai.pdfbox.preflight.content.PreflightContentStream;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.preflight.utils.COSUtils;
import org.allenai.pdfbox.preflight.utils.ContextHelper;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.pdmodel.PDPage;
import org.allenai.pdfbox.pdmodel.PDResources;
import org.allenai.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * This class validates XObject with the Form subtype.
 */
public class XObjFormValidator extends AbstractXObjValidator
{
    /**
     * High level object which represents the XObjectForm
     */
    PDFormXObject pdXObj = null;

    public XObjFormValidator(PreflightContext context, PDFormXObject xobj)
    {
        super(context, xobj.getCOSStream());
        this.pdXObj = xobj;
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
        checkGroup();
        checkSubtype2Value();
        validateXObjectResources();
        validateXObjectContent();
    }

    /*
     * (non-Javadoc)
     * 
     * @seenet.awl.edoc.pdfa.validation.graphics.AbstractXObjValidator# checkMandatoryFields(java.util.List)
     */
    @Override
    protected void checkMandatoryFields()
    {
        boolean lastMod = this.xobject.getItem(COSName.LAST_MODIFIED) != null;
        boolean pieceInfo = this.xobject.getItem("PieceInfo") != null;
        // type and subtype checked before to create the Validator.
        if (lastMod ^ pieceInfo)
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_FIELD));
            return;
        }

        COSBase bbBase = this.xobject.getItem(COSName.BBOX);
        // ---- BBox is an Array (Rectangle)
        if (bbBase == null || !COSUtils.isArray(bbBase, cosDocument))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_INVALID_BBOX));
            return;
        }
    }

    /**
     * An Form XObject is a ContentStream. This method method uses an instance of ContentStreamWrapper to check the
     * Stream of this Form XObject.
     */
    protected void validateXObjectContent() throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        PreflightContentStream csWrapper = new PreflightContentStream(context, vPath.getClosestPathElement(PDPage.class));
        csWrapper.validateXObjContentStream(pdXObj);
    }

    /**
     * A Form XObject may contain a Group object (Key =" Group"). If a Group object is present, this method checks if
     * the S entry is present and if its value is different from "Transparency".
     * 
     */
    protected void checkGroup()
    {
        COSBase baseGroup = this.xobject.getItem(PreflightConstants.XOBJECT_DICTIONARY_KEY_GROUP);
        COSDictionary groupDictionary = COSUtils.getAsDictionary(baseGroup, cosDocument);
        if (groupDictionary != null)
        {
            if (!PreflightConstants.XOBJECT_DICTIONARY_KEY_GROUP.equals(groupDictionary.getNameAsString(COSName.TYPE)))
            {
                context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_MISSING_FIELD, 
                        "The Group dictionary hasn't Group as Type value"));
            } 
            else 
            {
                String sVal = groupDictionary.getNameAsString(COSName.S);
                if (sVal == null || PreflightConstants.XOBJECT_DICTIONARY_VALUE_S_TRANSPARENCY.equals(sVal))
                {
                    context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_TRANSPARENCY_GROUP,
                            "Group has a transparency S entry or the S entry is null ["+xobject.toString()+"]"));
                    return;
                }
            }
        }
    }

    /**
     * Check if there are no PS entry in the Form XObject dictionary
     */
    protected void checkPS()
    {
        // 6.2.4 and 6.2.5 no PS
        if (this.xobject.getItem(COSName.getPDFName("PS")) != null)
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_KEY,
                    "Unexpected 'PS' Key"));
            return;
        }
    }

    /**
     * Check the SUbtype2 entry according to the §6.2.5 of the ISO 190005-1:2005 specification.
     */
    protected void checkSubtype2Value()
    {
        // 6.2.5 if Subtype2, value not PS
        if (this.xobject.getItem(COSName.getPDFName("Subtype2")) != null
                && "PS".equals(this.xobject.getNameAsString("Subtype2")))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_GRAPHIC_UNEXPECTED_VALUE_FOR_KEY,
                    "Unexpected 'PS' value for 'Subtype2' Key"));
            return;
        }
    }

    protected void validateXObjectResources() throws ValidationException
    {
        PDResources resources = this.pdXObj.getResources();
        if (resources != null)
        {
            ContextHelper.validateElement(context, resources, RESOURCES_PROCESS);
        }
    }

}