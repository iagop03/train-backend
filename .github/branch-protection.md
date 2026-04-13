# Branch Protection Configuration

## Configuración de Protección de Ramas

### Rama: `main`
- **Require pull request reviews before merging**: 1 reviewer
- **Require status checks to pass before merging**:
  - `build-and-test` (Maven build and tests)
  - `SonarQube Analysis`
- **Require branches to be up to date before merging**: ✓
- **Require code reviews**: ✓
- **Dismiss stale pull request approvals when new commits are pushed**: ✓
- **Require review from Code Owners**: ✓
- **Allow force pushes**: ✗
- **Allow deletions**: ✗
- **Require linear history**: ✓

### Rama: `develop`
- **Require pull request reviews before merging**: 1 reviewer
- **Require status checks to pass before merging**:
  - `build-and-test` (Maven build and tests)
- **Require branches to be up to date before merging**: ✓
- **Require code reviews**: ✓
- **Dismiss stale pull request approvals when new commits are pushed**: ✓
- **Require review from Code Owners**: ✗
- **Allow force pushes**: ✗
- **Allow deletions**: ✗
- **Require linear history**: ✓

## Instrucciones para Implementar

1. Ir a Settings > Branches
2. Añadir protección para `main`
3. Añadir protección para `develop`
4. Configurar las opciones según lo anterior

## Política de Merge

- Squash and merge para features individuales
- Create a merge commit para releases
- Rebase and merge para bugfixes
