package org.molgenis.omx.biobankconnect.wizard;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.ui.wizard.AbstractWizardPage;
import org.molgenis.ui.wizard.Wizard;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Component
public class AlgorithmReportPage extends AbstractWizardPage
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getTitle()
	{
		return "Harmonization Report";
	}

	@Override
	public String handleRequest(HttpServletRequest request, BindingResult result, Wizard wizard)
	{
		return null;
	}
}
