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

import java.util.List;

import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.PreflightPath;
import org.allenai.pdfbox.preflight.exception.ValidationException;
import org.allenai.pdfbox.cos.COSDictionary;
import org.allenai.pdfbox.pdmodel.PDPage;
import org.allenai.pdfbox.preflight.PreflightConfiguration;
import org.allenai.pdfbox.preflight.PreflightContext;
import org.allenai.pdfbox.preflight.action.AbstractActionManager;
import org.allenai.pdfbox.preflight.action.ActionManagerFactory;
import org.allenai.pdfbox.preflight.process.AbstractProcess;
import org.allenai.pdfbox.preflight.ValidationResult.ValidationError;

public class ActionsValidationProcess extends AbstractProcess
{

    @Override
    public void validate(PreflightContext context) throws ValidationException
    {
        PreflightPath vPath = context.getValidationPath();
        if (vPath.isEmpty()) 
        {
            return;
        }
        else if (!vPath.isExpectedType(COSDictionary.class))
        {
            context.addValidationError(new ValidationError(PreflightConstants.ERROR_ACTION_INVALID_TYPE, "Action validation process needs at least one COSDictionary object"));
        }
        else 
        {
            COSDictionary actionsDict = (COSDictionary) vPath.peek();
            // AA entry is authorized only for Page, in this case A Page is just before the Action Dictionary in the path
            boolean aaEntryAuth = ((vPath.size() - vPath.getClosestTypePosition(PDPage.class)) == 2);

            PreflightConfiguration config = context.getConfig();
            ActionManagerFactory factory = config.getActionFact();
            List<AbstractActionManager> la = factory.getActionManagers(context, actionsDict);
            for (AbstractActionManager aMng : la)
            {
                aMng.valid(aaEntryAuth);
            }
        }
    }

}
