import { expect, test, Page } from '@playwright/test'

test('Test information page', async ({ page }) => {
  await page.goto('/')
  await uploadFile('result_small_cluster.zip', page)

  // check displayed information on overview page
  const bodyOverview = await page.locator('body').textContent()
  expect(bodyOverview).toContain('Directory: files')
  expect(bodyOverview).toContain('Total Submissions: 4')
  expect(bodyOverview).toContain('Total Comparisons: 6')
  expect(bodyOverview).toContain('Min Match Length: 9')

  // go to information page
  await page.getByText('More').click()
  await page.waitForURL('/info')

  // check displayed run options on information page
  const runOptions = await page.getByText('Run Options:Submission Folder:').textContent()
  expect(runOptions).toContain('Submission Folder: files')
  expect(runOptions).toContain('Basecode Folder:')
  expect(runOptions).toContain('Language: Javac based AST plugin')
  expect(runOptions).toContain('File Extentions: .java, .JAVA')
  expect(runOptions).toContain('Minimum Token Match:: 9')

  const runData = await page.getByText('Run Data:Date of Execution:').textContent()
  expect(runData).toContain('Date of Execution: 02/09/23')
  expect(runData).toContain('Execution Duration: 12 ms')
  expect(runData).toContain('Submission Count: 4')
  expect(runData).toContain('Total Comparisons: 6')
  expect(runData).toContain('Shown Comparisons: 6')
  expect(runData).toContain('Missing Comparisons: 0')
})

export async function uploadFile(fileName: string, page: Page) {
  expect(page).toHaveURL('/')

  // upload file through file chooser
  const fileChooserPromise = page.waitForEvent('filechooser')
  await page.getByText('Drag and Drop zip/Json file on this page').click()
  const fileChooser = await fileChooserPromise
  await fileChooser.setFiles(`tests/e2e/assets/${fileName}`)

  await page.waitForURL('/overview')
}
