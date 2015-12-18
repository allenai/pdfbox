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

package org.allenai.pdfbox.preflight.action;

import org.allenai.pdfbox.cos.COSBase;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.ValidationResult;
import org.allenai.pdfbox.cos.COSName;

/**
 * ActionManager for the Submit action SubmitAction is valid if the F entry is present.
 */
public class SubmitAction extends AbstractActionManager
{

    /**
     * @param amFact Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict the COSDictionary of the action wrapped by this class.
     * @param ctx the preflight context.
     * @param aaKey the name of the key which identify the action in a additional action dictionary.
     */
    public SubmitAction(ActionManagerFactory amFact, COSDictionary adict, PreflightContext ctx, String aaKey)
    {
        super(amFact, adict, ctx, aaKey);
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.awl.edoc.pdfa.validation.actions.AbstractActionManager#valid(java.util .List)
     */
    @Override
    protected boolean innerValid()
    {
        COSBase f = this.actionDictionnary.getItem(COSName.F);
        if (f == null)
        {
            context.addValidationError(new ValidationResult.ValidationError(PreflightConstants.ERROR_ACTION_MISING_KEY,
                    "F entry is mandatory for the SubmitActions"));
            return false;
        }
        return true;
    }

}
