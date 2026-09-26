# -*- coding: utf-8 -*-
"""OpenPocketCine 中文界面重放器.

用法: python3 reapply.py [--root PATH] [--version-code N] [--rules rules.json]
在一份上游干净工作树上应用全部中文翻译规则。规则带源行号锚点:
优先在锚点 ±WIN 行窗口内替换一次; 窗口失配时退化为全文唯一性匹配
(仅当 old 在全文恰好出现一次); 仍不唯一则保守跳过 (保留英文, 不误伤).
"""
import argparse, json, os, re, sys

HERE = os.path.dirname(os.path.abspath(__file__))

ap = argparse.ArgumentParser()
ap.add_argument('--root', default='.', help='仓库根 (含 Apps/)')
ap.add_argument('--version-code', type=int, default=0, help='写入 gradle.properties 的 versionCode, 0=不改')
ap.add_argument('--rules', default=os.path.join(HERE, 'rules.json'))
ap.add_argument('--report', default='')
args = ap.parse_args()

rules = json.load(open(args.rules, encoding='utf-8'))
rules.sort(key=lambda r: -r.get('line', 0))   # 自底向上替换, 行号不漂移

WIN = 12
by_file = {}
for r in rules:
    by_file.setdefault(r['file'], []).append(r)

applied, skipped = [], []

def apply_rule(lines, r):
    old, new, ln = r['old'], r['new'], r.get('line', 0)
    n_old = old.count('\n') + 1
    lo, hi = max(0, ln - 1 - WIN), min(len(lines), ln - 1 + WIN)
    for idx in range(lo, hi):
        seg = '\n'.join(lines[idx:idx + n_old])
        if old in seg:
            lines[idx:idx + n_old] = seg.replace(old, new, 1).split('\n')
            return True
    # 全文兜底: 仅当 old 在全文恰好出现一次
    blob = '\n'.join(lines)
    if blob.count(old) == 1:
        head = blob[:blob.index(old)]
        start_line = head.count('\n')
        lines[start_line:start_line + n_old] = \
            '\n'.join(lines[start_line:start_line + n_old]).replace(old, new, 1).split('\n')
        return True
    return False

for rel, rs in by_file.items():
    path = os.path.join(args.root, rel)
    if not os.path.exists(path):
        skipped.append((rel, '<file gone>', '文件不存在(上游重构)'))
        continue
    lines = open(path, encoding='utf-8').read().split('\n')
    dirty = False
    for r in rs:
        if r['old'] == r['new']:
            continue
        if apply_rule(lines, r):
            applied.append(rel)
            dirty = True
        else:
            skipped.append((rel, r['old'][:60].replace('\n', '\\n'), '失配-保留英文'))
    if dirty:
        open(path, 'w', encoding='utf-8').write('\n'.join(lines))

vc_msg = ''
if args.version_code > 0:
    gp = os.path.join(args.root, 'Apps/Android/gradle.properties')
    src = open(gp, encoding='utf-8').read()
    new = re.sub(r'(?m)^openpocketcine\.versionCode=\d+$',
                 f'openpocketcine.versionCode={args.version_code}', src)
    if new != src:
        open(gp, 'w', encoding='utf-8').write(new)
        vc_msg = f', versionCode={args.version_code}'

print(f'applied: {len(applied)} rule-hits, skipped: {len(skipped)}{vc_msg}')
for rel, old, why in skipped:
    print(f'  SKIP {rel} | {old!r} | {why}', file=sys.stderr)

if args.report:
    json.dump({'applied': len(applied),
               'skipped': [{'file': r[0], 'old': r[1], 'why': r[2]} for r in skipped]},
              open(args.report, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
sys.exit(0)
