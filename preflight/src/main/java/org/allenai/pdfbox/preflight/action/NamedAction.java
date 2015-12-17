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

import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.cos.COSName;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;

/**
 * ActionManager for the Named action. Named action is valid if N entry is present with one of the four values :
 * <UL>
 * <li>NextPage
 * <li>PrevPage
 * <li>FirstPage
 * <li>LastPage
 * </UL>
 */
public class NamedAction extends AbstractActionManager
{

    /**
     * @param amFact Instance of ActionManagerFactory used to create ActionManager to check Next actions.
     * @param adict the COSDictionary of the action wrapped by this class.
     * @param ctx the preflight context.
     * @param aaKey the name of the key which identify the action in a additional action dictionary.
     */
    public NamedAction(ActionManagerFactory amFact, COSDictionary adict, PreflightContext ctx, String aaKey)
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
        String n = this.actionDictionnary.getNameAsString(COSName.N);

        // ---- N entry is mandatory
        if (n == null || "".equals(n))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_ACTION_MISING_KEY,
                    "N entry is mandatory for the NamedActions"));
            return false;
        }

        // ---- Only Predefine name actions are authorized
        if (!(PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_FIRST.equals(n) || PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_LAST.equals(n)
                || PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_NEXT.equals(n) || PreflightConstants.ACTION_DICTIONARY_VALUE_ATYPE_NAMED_PREV
                    .equals(n)))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_ACTION_FORBIDDEN_ACTIONS_NAMED, n
                    + " isn't authorized as named action"));
            return false;
        }

        return true;
    }
}
