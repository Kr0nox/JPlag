import iCoverage from 'istanbul-lib-coverage'
import iReport from 'istanbul-lib-report'
import { sync } from 'glob'
import { appendFileSync, existsSync, mkdirSync, readFileSync, rmSync } from 'fs'
import reports from 'istanbul-reports'

const coverageMap = iCoverage.createCoverageMap({})

const files = sync('*/coverage/coverage-final.json')

files.forEach((file) => {
  const coverage = JSON.parse(readFileSync(file, 'utf-8'))
  coverageMap.merge(coverage)
})

if (existsSync('coverage')) {
  rmSync('coverage', { recursive: true, force: true })
}
mkdirSync('coverage')

const context = iReport.createContext({
  dir: 'coverage',
  coverageMap
})

reports.create('text-summary', { file: 'summary.txt' }).execute(context)
reports.create('text', { file: 'detail.txt' }).execute(context)
reports.create('html', { subdir: 'html' }).execute(context)

// export summary for github actions to allow it to fail
const githubOutput = process.env.GITHUB_OUTPUT
console.log(githubOutput, process.env.GITHUB_OUTPUT)
if (githubOutput) {
  const oldContent = readFileSync(githubOutput)
  console.log('append')
  appendFileSync(githubOutput, `COVERAGE=${coverageMap.getCoverageSummary().lines.pct}\n`)
  const newContent = readFileSync(githubOutput)
  console.log(oldContent, newContent)
}
console.log(githubOutput, process.env.GITHUB_OUTPUT)

console.log('Merged Coverage Reports')
